require 'rspec'
require 'tempfile'

# Regression test for IO#pread/pwrite with offsets >2GB.
#
# pread and pwrite used int (32-bit) for the file offset parameter,
# truncating positions beyond ~2.1GB. CRuby uses off_t (64-bit).

describe "IO#pread and IO#pwrite with large offsets" do
  before(:each) do
    @file = Tempfile.new("pread_pwrite_large")
  end

  after(:each) do
    @file.close
  end

  it "pwrite writes at offsets > 2GB" do
    @file.pwrite("hello", 3_000_000_000)
    data = @file.pread(5, 3_000_000_000)
    expect(data).to eq("hello")
  end

  it "pread reads at offsets > 2GB" do
    @file.pwrite("world", 4_000_000_000)
    data = @file.pread(5, 4_000_000_000)
    expect(data).to eq("world")
  end

  it "still works with small offsets" do
    @file.pwrite("abc", 0)
    @file.pwrite("def", 3)
    expect(@file.pread(6, 0)).to eq("abcdef")
  end
end
