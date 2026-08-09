package org.jruby.runtime;

import com.headius.invokebinder.Binder;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class CompiledIRBlockBody extends IRBlockBody {
    protected final MethodHandle handle;
    protected final MethodHandle callHandle;
    protected final MethodHandle yieldDirectHandle;
    // arg0 (instead of IRubyObject[] args) entry for single-argument blocks, null when not eligible
    protected final MethodHandle oneValueHandle;
    protected final MethodHandle yieldOneValueDirectHandle;
    protected MethodHandle normalYieldHandle;
    protected MethodHandle normalYieldSpecificHandle;
    protected MethodHandle normalYieldUnwrapHandle;
    private final String encodedArgumentDescriptors;

    public CompiledIRBlockBody(MethodHandle handle, StaticScope scope, String file, int line, String encodedArgumentDescriptors, long encodedSignature) {
        this(handle, null, scope, file, line, encodedArgumentDescriptors, encodedSignature);
    }

    public CompiledIRBlockBody(MethodHandle handle, MethodHandle oneValueHandle, StaticScope scope, String file, int line, String encodedArgumentDescriptors, long encodedSignature) {
        super(scope, file, line, Signature.decode(encodedSignature));

        this.handle = handle;
        MethodHandle callHandle = MethodHandles.insertArguments(handle, 2, scope, null);
        // This is gross and should be done in IR rather than in the handles.
        this.callHandle = MethodHandles.foldArguments(callHandle, CHECK_ARITY);
        this.yieldDirectHandle = MethodHandles.insertArguments(
                MethodHandles.insertArguments(handle, 2, scope),
                4,
                Block.NULL_BLOCK);
        this.oneValueHandle = oneValueHandle;
        this.yieldOneValueDirectHandle = oneValueHandle == null ? null :
                MethodHandles.insertArguments(
                        MethodHandles.insertArguments(oneValueHandle, 2, scope),
                        4,
                        Block.NULL_BLOCK);

        // Done in the interpreter (WrappedIRClosure) but we do it here
        scope.determineModule();

        this.encodedArgumentDescriptors = encodedArgumentDescriptors;
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final MethodHandle VALUE_TO_ARRAY = Binder.from(IRubyObject[].class, IRubyObject.class).invokeStaticQuiet(LOOKUP, IRRuntimeHelpers.class, "singleBlockArgToArray");

    private static final MethodHandle WRAP_VALUE = Binder.from(IRubyObject[].class, IRubyObject.class).invokeStaticQuiet(LOOKUP, CompiledIRBlockBody.class, "wrapValue");

    private static final MethodHandle CHECK_ARITY = Binder
            .from(void.class, ThreadContext.class, Block.class, IRubyObject[].class, Block.class)
            .invokeStaticQuiet(LOOKUP, CompiledIRBlockBody.class, "checkArity");

    private static void checkArity(ThreadContext context, Block selfBlock, IRubyObject[] args, Block block) {
        if (selfBlock.type == Block.Type.LAMBDA) {
            selfBlock.getSignature().checkArity(context, args);
        }
    }

    private static IRubyObject[] wrapValue(IRubyObject value) { return new IRubyObject[] {value}; }

    @Override
    public ArgumentDescriptor[] getArgumentDescriptors() {
        return ArgumentDescriptor.decode(scope.getModule().getRuntime(), encodedArgumentDescriptors);
    }

    @Override
    public boolean canCallDirect() {
        return true;
    }

    public MethodHandle getCallHandle() {
        return callHandle;
    }

    public MethodHandle getNormalYieldSpecificHandle() {
        MethodHandle normalYieldSpecificHandle = this.normalYieldSpecificHandle;
        if (normalYieldSpecificHandle != null) return normalYieldSpecificHandle;

        return this.normalYieldSpecificHandle = Binder.from(IRubyObject.class, ThreadContext.class, Block.class)
                .append(new Class[] {StaticScope.class, IRubyObject.class, IRubyObject[].class, Block.class},
                        getStaticScope(), null, null, Block.NULL_BLOCK)
                .invoke(handle);
    }

    public MethodHandle getNormalYieldHandle() {
        MethodHandle normalYieldHandle = this.normalYieldHandle;
        if (normalYieldHandle != null) return normalYieldHandle;

        if (oneValueHandle != null) { // single arg blocks avoid the WRAP_VALUE boxing
            return this.normalYieldHandle = Binder.from(IRubyObject.class, ThreadContext.class, Block.class, IRubyObject.class)
                    .insert(2, new Class[]{StaticScope.class, IRubyObject.class}, getStaticScope(), null)
                    .append(Block.class, Block.NULL_BLOCK)
                    .invoke(oneValueHandle);
        }

        return this.normalYieldHandle = Binder.from(IRubyObject.class, ThreadContext.class, Block.class, IRubyObject.class)
                .filter(2, WRAP_VALUE)
                .insert(2, new Class[]{StaticScope.class, IRubyObject.class}, getStaticScope(), null)
                .append(Block.class, Block.NULL_BLOCK)
                .invoke(handle);
    }

    public MethodHandle getNormalYieldUnwrapHandle() {
        MethodHandle normalYieldUnwrapHandle = this.normalYieldUnwrapHandle;
        if (normalYieldUnwrapHandle != null) return normalYieldUnwrapHandle;

        return this.normalYieldUnwrapHandle = Binder.from(IRubyObject.class, ThreadContext.class, Block.class, IRubyObject.class)
                .filter(2, VALUE_TO_ARRAY)
                .insert(2, new Class[] {StaticScope.class, IRubyObject.class}, getStaticScope(), null)
                .append(Block.class, Block.NULL_BLOCK)
                .invoke(handle);
    }

    @Override
    protected IRubyObject callDirect(ThreadContext context, Block block, IRubyObject[] args, Block blockArg) {
        try {
            return (IRubyObject) callHandle.invokeExact(context, block, args, blockArg);
        } catch (Throwable t) {
            Helpers.throwException(t);
            return null; // not reached
        }
    }

    @Override
    protected IRubyObject yieldDirect(ThreadContext context, Block block, IRubyObject[] args, IRubyObject self) {
        try {
            return (IRubyObject) yieldDirectHandle.invokeExact(context, block, self, args);
        } catch (Throwable t) {
            Helpers.throwException(t);
            return null; // not reached
        }
    }

    @Override
    protected IRubyObject yieldDirect(ThreadContext context, Block block, IRubyObject value, IRubyObject self) {
        MethodHandle yieldOneValue = this.yieldOneValueDirectHandle;
        if (yieldOneValue == null) return super.yieldDirect(context, block, value, self); // boxes

        try {
            return (IRubyObject) yieldOneValue.invokeExact(context, block, self, value);
        } catch (Throwable t) {
            Helpers.throwException(t);
            return null; // not reached
        }
    }

    @Override
    protected IRubyObject commonYieldPath(ThreadContext context, Block block, IRubyObject[] args, IRubyObject self, Block blockArg) {
        throw new UnsupportedOperationException("commonYieldPath not implemented");
    }

}
