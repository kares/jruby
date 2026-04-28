package org.jruby.java.proxies;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.ir.JIT;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class BlockInterfaceTemplate implements RubyObjectHolderProxy {
    private final RubyProc proc;
    private final Block block;

    public BlockInterfaceTemplate(final RubyProc proc) {
        assert proc != null;
        this.proc = proc;
        this.block = proc.getBlock();
    }

    public IRubyObject __ruby_object() {
        return proc;
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType) {
        final Ruby runtime = proc.getRuntime();

        final IRubyObject result = block.call(runtime.getCurrentContext());

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object arg0) {
        final Ruby runtime = proc.getRuntime();

        final IRubyObject rubyArg = JavaUtil.convertJavaToUsableRubyObject(runtime, arg0);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArg);

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object arg0, Object arg1) {
        final Ruby runtime = proc.getRuntime();

        final IRubyObject rubyArg1 = JavaUtil.convertJavaToUsableRubyObject(runtime, arg0);
        final IRubyObject rubyArg2 = JavaUtil.convertJavaToUsableRubyObject(runtime, arg1);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArg1, rubyArg2);

        return returnType == void.class ? null : result.toJava(returnType);
    }

    @JIT
    @SuppressWarnings("unused")
    protected final Object __ruby_call(final Class<?> returnType, Object... args) {
        final Ruby runtime = proc.getRuntime();

        final IRubyObject[] rubyArgs = JavaUtil.convertJavaArrayToRuby(runtime, args);
        final IRubyObject result = block.call(runtime.getCurrentContext(), rubyArgs);

        return returnType == void.class ? null : result.toJava(returnType);
    }
}
