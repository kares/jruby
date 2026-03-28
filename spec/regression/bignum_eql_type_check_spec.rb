require 'rspec'

# Regression test for Bignum#eql? type-strict semantics.
#
# Ruby's eql? must be type-strict: it should return false when
# comparing an Integer with a Float or Rational, even if they
# represent the same numeric value. The bug was that RubyBignum
# delegated eql? to == which accepts cross-type comparisons.

describe "Bignum#eql?" do
  it "returns true for same-value Bignums" do
    expect((2**100).eql?(2**100)).to be true
  end

  it "returns false for different-value Bignums" do
    expect((2**100).eql?(2**100 + 1)).to be false
  end

  it "returns false for Float with same numeric value" do
    expect((2**100).eql?((2**100).to_f)).to be false
  end

  it "returns false for Rational with same numeric value" do
    expect((2**100).eql?(Rational(2**100))).to be false
  end

  it "returns false for Fixnum-range Integer" do
    expect((2**100).eql?(42)).to be false
  end

  it "does not affect == which remains type-loose" do
    expect((2**100) == (2**100).to_f).to be true
    expect((2**100) == Rational(2**100)).to be true
  end
end
