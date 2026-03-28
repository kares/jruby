require 'rspec'

# Regression test for Bignum <=> and == with Float precision.
#
# When comparing a large Bignum against a Float, the comparison must
# not lose precision by converting the Bignum to double. For example,
# 2**64 + 1 cannot be exactly represented as a double (it rounds to
# 2**64), so (2**64 + 1) <=> (2**64).to_f must return 1, not 0.
#
# The bug was that op_cmp used dbl_cmp(big2dbl(this), flt.value)
# which lost precision, and op_equal used big2dbl comparison which
# had the same problem.

describe "Bignum comparison with Float precision" do
  context "<=>" do
    it "returns 1 when Bignum is greater than Float despite double rounding" do
      expect((2**64 + 1) <=> (2**64).to_f).to eq 1
      expect((2**100 + 1) <=> (2**100).to_f).to eq 1
    end

    it "returns 0 when Bignum exactly equals Float" do
      expect((2**64) <=> (2**64).to_f).to eq 0
    end

    it "returns -1 when Bignum is less than Float" do
      expect((2**64 - 1) <=> (2**64).to_f).to eq(-1)
    end

    it "returns nil for NaN" do
      expect((2**100) <=> Float::NAN).to be_nil
    end

    it "handles Infinity correctly" do
      expect((2**100) <=> Float::INFINITY).to eq(-1)
      expect((2**100) <=> -Float::INFINITY).to eq 1
    end
  end

  context "==" do
    it "returns false when Bignum differs from Float due to precision" do
      expect((2**64 + 1) == (2**64).to_f).to be false
      expect((2**100 + 1) == (2**100).to_f).to be false
    end

    it "returns true when Bignum exactly equals Float" do
      expect((2**64) == (2**64).to_f).to be true
    end

    it "returns false for NaN and Infinity" do
      expect((2**100) == Float::NAN).to be false
      expect((2**100) == Float::INFINITY).to be false
    end
  end
end
