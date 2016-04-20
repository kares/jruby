# NOTE: these Ruby extensions were moved to native code!
# @see org.jruby.javasupport.ext.JavaUtil.java
# this file is no longer loaded but is kept to provide doc stubs

# *java.util.Collection* is enhanced (not just) to act like Ruby's `Enumerable`.
# @see http://docs.oracle.com/javase/8/docs/api/java/util/Collection.html
module Java::java::util::Collection
  include ::Enumerable

  def each
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # i = iterator
    # while i.hasNext
    #   yield i.next
    # end
  end

  def <<(a)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # add(a)
    # self
  end

  def +(oth)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # nw = self.dup
    # nw.addAll(oth)
    # nw
  end

  def -(oth)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # nw = self.dup
    # nw.removeAll(oth)
    # nw
  end

  alias length size

  # @private Not sure if this makes sense to have.
  def join(*args)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

  def to_a
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

end if false

# A *java.util.Enumeration* instance might be iterated Ruby style.
# @see http://docs.oracle.com/javase/8/docs/api/java/util/Enumeration.html
module Java::java::util::Enumeration
  include ::Enumerable

  def each
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # while hasMoreElements
    #   yield nextElement
    # end
  end
end if false

# A *java.util.Iterator* acts like an `Enumerable`.
# @see http://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html
module Java::java::util::Iterator
  include ::Enumerable

  def each
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
    # while hasNext
    #   yield next
    # end
  end
end if false

# Ruby extensions for *java.util.List* instances.
# @see Java::java::util::Collection
# @see http://docs.oracle.com/javase/8/docs/api/java/util/List.html
module Java::java::util::List
  # @private
  module RubyComparators
    class BlockComparator
      include java::util::Comparator

      def initialize(block)
        @block = block
      end

      def compare(o1, o2)
        @block.call(o1, o2)
      end
    end
    class SpaceshipComparator
      include java::util::Comparator
      def compare(o1, o2)
        o1 <=> o2
      end
    end
  end
  private_constant :RubyComparators

  def [](i1, i2 = nil)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

  def []=(i, val)
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

  def index(obj = (no_args = true))
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

  def rindex(obj = (no_args = true))
    # stub implemented in org.jruby.javasupport.ext.JavaUtil.java
  end

  def sort(&block)
    # comparator = block ? RubyComparators::BlockComparator.new(block) : RubyComparators::SpaceshipComparator.new
    # list = self.dup
    # java::util::Collections.sort(list, comparator)
    # list
  end

  def sort!(&block)
    # comparator = block ? RubyComparators::BlockComparator.new(block) : RubyComparators::SpaceshipComparator.new
    # java::util::Collections.sort(self, comparator)
    # self
  end

  alias_method :to_ary, :to_a

end if false

# Ruby extensions for *java.util.Map* instances.
# Generally maps behave like Ruby's `Hash` objects.
# @see http://docs.oracle.com/javase/8/docs/api/java/util/Map.html
module Java::java::util::Map

  def default(arg = nil)
    # stub
  end

  def default=(value)
    # stub
  end

  def default_proc()
    # stub
  end

  def default_proc=(proc)
    # stub
  end

  alias size length

  def empty?
    # stub
  end

  # @return [Array]
  def to_a
    # stub
  end

  # @return [Proc]
  def to_proc
    # stub
  end

  # @return [Hash]
  def to_h
    # stub
  end
  alias to_hash to_h

  def [](key)
    # stub
  end

  def []=(key, value)
    # stub
  end
  alias store []

  def fetch(key, default = nil, &block)
    # stub
  end

  def key?(key)
    # stub
  end
  alias has_key? key?
  alias include? key?
  alias member? key?

  def value?(value)
    # stub
  end
  alias has_value? value?

  def each(&block)
    # stub
  end
  alias each_pair each

  def each_key(&block)
    # stub
  end

  def each_value(&block)
    # stub
  end

  def ==(other)
    # stub
  end

  def <(other)
    # stub
  end

  def <=(other)
    # stub
  end

  def >(other)
    # stub
  end

  def >=(other)
    # stub
  end

  def select(&block)
    # stub
  end

  def select!(&block)
    # stub
  end

  def keep_if(&block)
    # stub
  end

  def sort(&block)
    # stub
  end

  def delete(key, &block)
    # stub
  end

  def delete_if(&block)
    # stub
  end

  def reject(&block)
    # stub
  end

  def reject!(&block)
    # stub
  end

  def invert
    # stub
  end

  def key(value)
    # stub
  end

  def keys
    # stub
  end

  def ruby_values
    # stub
  end
  alias values ruby_values

  def values_at(*args)
    # stub
  end

  def fetch_values(*args)
    # stub
  end

  def ruby_clear
    # stub
  end
  alias clear ruby_clear

  def ruby_merge(other, &block)
    # stub
  end
  alias merge ruby_merge

  def merge!(other, &block)
    # stub
  end

  def ruby_replace(other)
    # stub
  end
  alias replace ruby_replace

  def flatten(level = nil)
    # stub
  end

  def assoc(obj)
    # stub
  end

  def rassoc(obj)
    # stub
  end

  def any?(&block)
    # stub
  end

  def dig(*args)
    # stub
  end

end if false