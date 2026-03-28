require 'rspec'

# Regression test for MatchData#deconstruct_keys with non-participating
# named captures.
#
# When a named capture group doesn't participate in the match (e.g.
# an optional group like (?<b>world)?), deconstruct_keys should include
# the key with a nil value, not omit it entirely.

describe "MatchData#deconstruct_keys with non-participating captures" do
  let(:match) { "hello".match(/(?<a>hello)(?<b>world)?/) }

  it "includes non-participating captures as nil for nil argument" do
    result = match.deconstruct_keys(nil)
    expect(result).to eq({ a: "hello", b: nil })
  end

  it "includes non-participating captures as nil for explicit keys" do
    result = match.deconstruct_keys([:a, :b])
    expect(result).to eq({ a: "hello", b: nil })
  end

  it "does not stop iterating at non-participating captures" do
    m = "hello!".match(/(?<a>hello)(?<b>world)?(?<c>!)/)
    result = m.deconstruct_keys([:b, :c, :a])
    expect(result).to eq({ b: nil, c: "!", a: "hello" })
  end

  it "preserves key order from argument" do
    result = match.deconstruct_keys([:b, :a])
    expect(result.keys).to eq([:b, :a])
  end
end
