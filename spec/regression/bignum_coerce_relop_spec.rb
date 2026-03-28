require 'rspec'

# Regression test for RubyBignum.big_op coercion.
#
# When a Bignum is compared against a custom numeric type that
# implements coerce, the coerced comparison must use the correct
# operator (>, >=, <, <=). The bug was that all four operators
# used > after coercion, so < and <= returned wrong results.

describe "Bignum comparison with coerce" do
  before :all do
    @cls = Class.new do
      def initialize(v) @v = v end
      def coerce(other) [self.class.new(other), self] end
      def >(other)  @v.to_i > other.to_i  end
      def >=(other) @v.to_i >= other.to_i end
      def <(other)  @v.to_i < other.to_i  end
      def <=(other) @v.to_i <= other.to_i end
      def to_i()    @v.to_i               end
    end
  end

  it "uses > after coercion for >" do
    expect((2**100) > @cls.new(1)).to be true
    expect((2**100) > @cls.new(2**101)).to be false
  end

  it "uses >= after coercion for >=" do
    expect((2**100) >= @cls.new(1)).to be true
    expect((2**100) >= @cls.new(2**101)).to be false
  end

  it "uses < after coercion for <" do
    expect((2**100) < @cls.new(2**101)).to be true
    expect((2**100) < @cls.new(1)).to be false
  end

  it "uses <= after coercion for <=" do
    expect((2**100) <= @cls.new(2**101)).to be true
    expect((2**100) <= @cls.new(1)).to be false
  end
end
