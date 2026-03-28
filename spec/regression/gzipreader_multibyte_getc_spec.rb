require 'rspec'
require 'zlib'
require 'stringio'

# Regression test for GzipReader#getc, #readchar, and #each_char
# with multibyte encodings.
#
# Previously these methods read a single byte per call, splitting
# multibyte UTF-8 characters into separate invalid characters.

describe "Zlib::GzipReader with multibyte characters" do
  def gzip(str)
    sio = StringIO.new("".b)
    gz = Zlib::GzipWriter.new(sio)
    gz.write(str)
    gz.close
    sio.string
  end

  def new_reader(compressed)
    Zlib::GzipReader.new(StringIO.new(compressed, "rb"))
  end

  describe "#getc" do
    it "returns full multibyte characters" do
      reader = new_reader(gzip("héllo 世界"))
      chars = []
      while (c = reader.getc)
        chars << c
      end
      reader.close

      expect(chars).to eq ["h", "é", "l", "l", "o", " ", "世", "界"]
    end

    it "returns characters with the correct encoding" do
      reader = new_reader(gzip("é"))
      c = reader.getc
      reader.close

      expect(c.encoding.name).to eq "UTF-8"
    end

    it "handles 4-byte UTF-8 characters" do
      reader = new_reader(gzip("a😀b"))
      chars = []
      while (c = reader.getc)
        chars << c
      end
      reader.close

      expect(chars).to eq ["a", "😀", "b"]
      expect(chars[1].bytesize).to eq 4
    end

    it "returns nil at EOF" do
      reader = new_reader(gzip(""))
      expect(reader.getc).to be_nil
      reader.close
    end

    it "works with ASCII content" do
      reader = new_reader(gzip("abc"))
      chars = []
      while (c = reader.getc)
        chars << c
      end
      reader.close

      expect(chars).to eq ["a", "b", "c"]
    end
  end

  describe "#readchar" do
    it "returns a String, not a Fixnum" do
      reader = new_reader(gzip("aé"))
      c = reader.readchar

      expect(c).to be_a(String)
      expect(c).to eq "a"
      reader.close
    end

    it "returns full multibyte characters" do
      reader = new_reader(gzip("aé"))
      reader.readchar # skip 'a'
      c = reader.readchar
      reader.close

      expect(c).to eq "é"
    end

    it "raises EOFError at end of stream" do
      reader = new_reader(gzip("a"))
      reader.readchar
      expect { reader.readchar }.to raise_error(EOFError)
      reader.close
    end
  end

  describe "#each_char" do
    it "yields full multibyte characters" do
      reader = new_reader(gzip("aé世"))
      chars = []
      reader.each_char { |c| chars << c }
      reader.close

      expect(chars).to eq ["a", "é", "世"]
    end
  end
end
