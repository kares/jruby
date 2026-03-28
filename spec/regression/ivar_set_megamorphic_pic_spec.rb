require 'rspec'

# Regression test for VariableSite.ivarSet megamorphic PIC handling.
#
# When a single ivar-set call site sees more class types than the
# invokedynamic.maxpoly threshold, it should demote to a megamorphic
# fallback (ivarSetFail) and stay there.
#
# The bug (fixed): after invoking the megamorphic fallback, ivarSet
# fell through and overwrote the call site target with a monomorphic
# guarded handle, defeating the megamorphic demotion. This caused:
#   1. The ivar to be set twice per call (once via fallback, once via
#      the newly-installed guarded handle's direct setter)
#   2. An unnecessary type guard on every subsequent call
#
# The primary effect was performance degradation (extra guard check on
# every call to the megamorphic site). Correctness was preserved by
# accident since the double-set is idempotent for the same value.
#
# These tests guard against future regressions where the double-set
# or PIC-state corruption could cause observable correctness issues,
# especially under concurrent access.
#
# The PIC state bug is directly observable via:
#   -Xinvokedynamic.log.binding=true
# A megamorphic site should log "failed (polymorphic)" without a
# subsequent "bound directly" for the same type.

describe "Instance variable set at a megamorphic call site" do
  it "correctly assigns ivars across many distinct receiver classes" do
    mod = Module.new do
      def set_x(v)
        @x = v
      end

      def get_x
        @x
      end
    end

    # 20 classes exceeds default maxpoly=6 — forces megamorphic path
    classes = (1..20).map { Class.new { include mod } }
    objs = classes.map(&:new)

    objs.each_with_index { |o, i| o.set_x(i * 7) }

    objs.each_with_index do |o, i|
      expect(o.get_x).to eq(i * 7)
    end
  end

  it "correctly assigns ivars under concurrent megamorphic pressure" do
    mod = Module.new do
      def set_x(v)
        @x = v
      end

      def get_x
        @x
      end
    end

    classes = (1..20).map { Class.new { include mod } }

    results = Array.new(20)
    threads = classes.each_with_index.map do |cls, i|
      Thread.new do
        obj = cls.new
        # Repeated assignments exercise the PIC under contention.
        100.times { |n| obj.set_x(i * 1000 + n) }
        results[i] = [obj, i * 1000 + 99]
      end
    end

    threads.each(&:join)

    results.each_with_index do |(obj, expected), i|
      expect(obj.get_x).to eq(expected)
    end
  end
end
