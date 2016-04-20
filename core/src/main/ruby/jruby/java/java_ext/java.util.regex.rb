# NOTE: these Ruby extensions were moved to native code!
# @see org.jruby.javasupport.ext.JavaUtilRegex.java
# this file is no longer loaded but is kept to provide doc stubs

class Java::java::util::regex::Pattern
  def =~(str)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
    # m = self.matcher(str)
    # m.find ? m.start : nil
  end
  
  def ===(str)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
    # self.matcher(str).find
  end

  def match(str)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
    # m = self.matcher(str)
    # m.str = str
    # m.find ? m : nil
  end
end if false

class Java::java::util::regex::Matcher
  # @private
  attr_accessor :str
  
  def captures
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def [](*args)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end

  def begin(ix)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def end(ix)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def to_a
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def size
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  alias length size
  
  def values_at(*args)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end

  def select
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def offset(ix)
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end

  def pre_match
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def post_match
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end
  
  def string
    # stub implemented in org.jruby.javasupport.ext.JavaUtilRegex.java
  end

end if false
