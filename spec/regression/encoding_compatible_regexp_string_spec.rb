require 'rspec'

# Regression test for Encoding.compatible? with Regexp and String.
#
# When comparing a Regexp (non-String) and a String with different
# encodings, the method should return the correct compatible encoding
# based on argument order. The bug was that the encoding values were
# swapped along with the objects, causing the wrong encoding to be
# returned when the Regexp was the first argument.

describe "Encoding.compatible? with Regexp and String" do
  it "returns the String's encoding when Regexp is first and String is ASCII-only" do
    r = Regexp.new("\xa4\xa2".force_encoding("EUC-JP"))
    s = "hello" # UTF-8, ASCII-only

    expect(Encoding.compatible?(r, s)).to eq(Encoding::UTF_8)
  end

  it "returns the Regexp's encoding when String is first and ASCII-only" do
    r = Regexp.new("\xa4\xa2".force_encoding("EUC-JP"))
    s = "hello"

    expect(Encoding.compatible?(s, r)).to eq(Encoding::EUC_JP)
  end

  it "returns the shared encoding when both use the same encoding" do
    r = Regexp.new("\xa4\xa2".force_encoding("EUC-JP"))
    s = "hello".encode("EUC-JP")

    expect(Encoding.compatible?(r, s)).to eq(Encoding::EUC_JP)
  end
end
