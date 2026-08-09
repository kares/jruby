package org.jruby.runtime;

import org.jruby.RubyModule;
import org.jruby.compiler.Compilable;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRScope;
import org.jruby.util.cli.Options;

import static org.jruby.api.Access.instanceConfig;

/**
 * A block body that counts yields and promotes itself to an optimized form once the JIT threshold
 * is crossed - a full build for the plain interpreter, a jitted body for mixed mode.
 */
public abstract class CompilableIRBlockBody<T> extends IRBlockBody implements Compilable<T> {

    protected final IRClosure closure;
    protected final boolean pushScope;
    protected final boolean reuseParentScope;
    protected boolean displayedCFG = false; // FIXME: Remove when we find nicer way of logging CFG
    protected int callCount = 0;

    protected CompilableIRBlockBody(IRClosure closure, Signature signature) {
        super(closure, signature);
        this.pushScope = true;
        this.reuseParentScope = false;
        this.closure = closure;
    }

    @Override
    public void setCallCount(int callCount) {
        synchronized (this) {
            this.callCount = callCount;
        }
    }

    @Override
    public boolean isBuildComplete() {
        // Successful build and disabled build both set callCount to -1, indicating no further build is possible.
        return callCount < 0;
    }

    @Override
    public boolean forceBuild(ThreadContext context) {
        promoteToFullBuild(context, true);

        // Force = true should trigger jit to run synchronously, so we'll be optimistic
        return true;
    }

    protected final void tryJIT(ThreadContext context) {
        // don't JIT during runtime boot
        if (callCount >= 0 && (!context.runtime.isBooting() || Options.JIT_KERNEL.load())) {
            // we don't synchronize callCount++ it does not matter if count isn't accurate
            if (callCount++ >= instanceConfig(context).getJitThreshold()) {
                promoteToFullBuild(context, false);
            }
        }
    }

    protected abstract void promoteToFullBuild(ThreadContext context, boolean force);

    @Override
    public IRScope getIRScope() {
        return closure;
    }

    @Override
    public IRClosure getScope() {
        return closure;
    }

    @Override
    public RubyModule getImplementationClass() {
        return closure.getStaticScope().getModule();
    }

    @Override
    public ArgumentDescriptor[] getArgumentDescriptors() {
        return closure.getArgumentDescriptors();
    }
}
