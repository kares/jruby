# NOTE: these Ruby extensions were moved to native code!
# @see org.jruby.javasupport.ext.JavaNet.java
# this file is no longer loaded but is kept to provide doc stubs

class Java::java::net::URL
  def open(&block)
    # stub implemented in org.jruby.javasupport.ext.JavaNet.java
    # stream = openStream
    # io = stream.to_io
    # if block
    #   begin
    #     block.call(io)
    #   ensure
    #     stream.close
    #   end
    # else
    #   io
    # end
  end
end if false
