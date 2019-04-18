package org.jruby.javasupport.ext;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.anno.JRubyMethod;
import org.jruby.anno.JRubyModule;
import org.jruby.ext.bigdecimal.RubyBigDecimal;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import static org.jruby.javasupport.JavaUtil.unwrapIfJavaObject;

/**
 * Java::JavaMath package extensions.
 *
 * @author kares
 */
public class JavaMath {

    public static void define(final Ruby runtime) {
        JavaExtensions.put(runtime, java.math.BigDecimal.class, (proxyClass) -> BigDecimal.define(runtime, proxyClass));
    }

    @JRubyModule(name = "Java::JavaMath::BigDecimal")
    public static class BigDecimal {

        static RubyModule define(final Ruby runtime, final RubyModule proxy) {
            proxy.defineAnnotatedMethods(BigDecimal.class);
            return proxy;
        }

        @JRubyMethod(name = "to_d") // bigdecimal/util.rb
        public static IRubyObject to_d(ThreadContext context, IRubyObject self) {
            return asRubyBigDecimal(context.runtime, unwrapIfJavaObject(self));
        }

        @JRubyMethod(name = "to_f") // override from java.lang.Number
        public static IRubyObject to_f(ThreadContext context, IRubyObject self) {
            return asRubyBigDecimal(context.runtime, unwrapIfJavaObject(self)).to_f();
        }

        @JRubyMethod(name = { "to_i", "to_int" }) // override from java.lang.Number
        public static IRubyObject to_i(ThreadContext context, IRubyObject self) {
            return asRubyBigDecimal(context.runtime, unwrapIfJavaObject(self)).to_int(context);
        }

        @JRubyMethod(name = "coerce") // override from java.lang.Number
        public static IRubyObject coerce(final ThreadContext context, final IRubyObject self, final IRubyObject type) {
            return context.runtime.newArray(type, asRubyBigDecimal(context.runtime, unwrapIfJavaObject(self)));
        }

        @JRubyMethod(name = "to_r")
        public static IRubyObject to_r(ThreadContext context, IRubyObject self) {
            return asRubyBigDecimal(context.runtime, unwrapIfJavaObject(self)).to_r(context);
        }

        private static RubyBigDecimal asRubyBigDecimal(final Ruby runtime, final java.math.BigDecimal value) {
            final RubyClass klass = runtime.getClass("BigDecimal");
            if (klass == null) { // user should require 'bigdecimal'
                throw runtime.newNameError("uninitialized constant BigDecimal", "BigDecimal");
            }
            return new RubyBigDecimal(runtime, klass, value);
        }

    }

}