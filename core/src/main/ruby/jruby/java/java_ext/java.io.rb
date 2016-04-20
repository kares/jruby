# NOTE: these Ruby extensions were moved to native code!
# @see org.jruby.javasupport.ext.JavaIo.java
# this file is no longer loaded but is kept to provide doc stubs

class Java::java::io::InputStream
  # Convert a stream to a Ruby `IO`
  # @option opts [Types] autoclose (nil) sets `IO#autoclose=`
  # @return [IO]
  def to_io(opts = nil)
    # stub implemented in org.jruby.javasupport.ext.JavaIo.java
  end
end if false

class Java::java::io::OutputStream
  # Convert a stream to a Ruby `IO`
  # @option opts [Types] autoclose (nil) sets `IO#autoclose=`
  # @return [IO]
  def to_io(opts = nil)
    # stub implemented in org.jruby.javasupport.ext.JavaIo.java
  end
end if false

module Java::java::nio::channels::Channel
  # @return [IO]
  def to_io(opts = nil)
    # stub implemented in org.jruby.javasupport.ext.JavaIo.java
  end
end if false
