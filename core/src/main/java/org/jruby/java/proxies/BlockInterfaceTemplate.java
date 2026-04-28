package org.jruby.java.proxies;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.ir.JIT;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class BlockInterfaceTemplate {
    private final Ruby runtime;
    private final Block block;

    public BlockInterfaceTemplate(final RubyProc proc) {
        assert proc != null;
        this.runtime = proc.getRuntime();
        this.block = proc.getBlock();
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType) {
        final IRubyObject result = block.call(runtime.getCurrentContext());

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object arg0) {
        final IRubyObject rubyArg = JavaUtil.convertJavaToUsableRubyObject(runtime, arg0);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArg);

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object arg0, Object arg1) {
        final IRubyObject rubyArg1 = JavaUtil.convertJavaToUsableRubyObject(runtime, arg0);
        final IRubyObject rubyArg2 = JavaUtil.convertJavaToUsableRubyObject(runtime, arg1);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArg1, rubyArg2);

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object... args) {
        final IRubyObject[] rubyArgs = JavaUtil.convertJavaArrayToRuby(runtime, args);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArgs);

        return returnType == void.class ? null : result.toJava(returnType);
    }
}
