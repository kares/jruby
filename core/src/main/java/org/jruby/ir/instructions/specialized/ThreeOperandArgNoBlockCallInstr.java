package org.jruby.ir.instructions.specialized;

import org.jruby.RubySymbol;
import org.jruby.ir.IRScope;
import org.jruby.ir.Operation;
import org.jruby.ir.instructions.CallInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.operands.NullBlock;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.CallType;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class ThreeOperandArgNoBlockCallInstr extends CallInstr {
    // normal constructor
    public ThreeOperandArgNoBlockCallInstr(IRScope scope, CallType callType, Variable result, RubySymbol name,
                                           Operand receiver, Operand[] args, int flags, boolean isPotentiallyRefined) {
        super(scope, Operation.CALL_3O, callType, result, name, receiver, args, NullBlock.INSTANCE, flags, isPotentiallyRefined);
    }

    @Override
    public Instr clone(CloneInfo ii) {
        return new ThreeOperandArgNoBlockCallInstr(ii.getScope(), getCallType(), ii.getRenamedVariable(result), getName(),
                getReceiver().cloneForInlining(ii), cloneCallArgs(ii), getFlags(), isPotentiallyRefined()
        );
    }

    public Operand getArg2() {
        return operands[2];
    }

    public Operand getArg3() {
        return operands[3];
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope dynamicScope, IRubyObject self, Object[] temp) {
        IRubyObject object = (IRubyObject) getReceiver().retrieve(context, self, currScope, dynamicScope, temp);
        IRubyObject arg1 = (IRubyObject) getArg1().retrieve(context, self, currScope, dynamicScope, temp);
        IRubyObject arg2 = (IRubyObject) getArg2().retrieve(context, self, currScope, dynamicScope, temp);
        IRubyObject arg3 = (IRubyObject) getArg3().retrieve(context, self, currScope, dynamicScope, temp);

        IRRuntimeHelpers.setCallInfo(context, getFlags());

        return getCallSite().call(context, self, object, arg1, arg2, arg3);
    }
}
