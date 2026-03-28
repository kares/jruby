require 'rspec'
require 'tempfile'

# Regression test for IO#reopen not copying encoding settings.
#
# When IO#reopen is called with another IO object, the encoding
# configuration (external, internal, ecflags) should be copied
# from the source IO. The bug was that reopenIO copied mode, path,
# process, lineNumber, and finalizer — but not the encoding config.

describe "IO#reopen with an IO" do
  it "copies the external and internal encoding from the source IO" do
    f1 = Tempfile.new("reopen_enc1")
    f2 = Tempfile.new("reopen_enc2")
    begin
      f1.set_encoding("EUC-JP:UTF-8")
      f2.set_encoding("ISO-8859-1:US-ASCII")

      f1.reopen(f2)

      expect(f1.external_encoding).to eq(Encoding::ISO_8859_1)
      expect(f1.internal_encoding).to eq(Encoding::US_ASCII)
    ensure
      f1.close
      f2.close
    end
  end

  it "copies external encoding when no internal encoding is set" do
    f1 = Tempfile.new("reopen_enc3")
    f2 = Tempfile.new("reopen_enc4")
    begin
      f1.set_encoding("EUC-JP")
      f2.set_encoding("ISO-8859-1")

      f1.reopen(f2)

      expect(f1.external_encoding).to eq(Encoding::ISO_8859_1)
      expect(f1.internal_encoding).to be_nil
    ensure
      f1.close
      f2.close
    end
  end
end
