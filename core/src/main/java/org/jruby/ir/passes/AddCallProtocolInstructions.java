package org.jruby.ir.passes;

import org.jruby.ir.*;
import org.jruby.ir.instructions.*;
import org.jruby.ir.interpreter.FullInterpreterContext;
import org.jruby.runtime.Signature;
import org.jruby.ir.operands.ImmutableLiteral;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Self;
import org.jruby.ir.operands.TemporaryVariable;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.representations.BasicBlock;
import org.jruby.ir.representations.CFG;
import org.jruby.runtime.Visibility;

import java.util.ListIterator;

public class AddCallProtocolInstructions extends CompilerPass {
    @Override
    public String getLabel() {
        return "Add Call Protocol Instructions (push/pop of dyn-scope, frame, impl-class values)";
    }

    @Override
    public String getShortLabel() {
        return "Add Call Proto";
    }

    private boolean explicitCallProtocolSupported(IRScope scope) {
        return scope instanceof IRMethod
                || (scope instanceof IRClosure && !(scope instanceof IREvalScript))
                || (scope instanceof IRModuleBody && !(scope instanceof IRMetaClassBody)
                || (scope instanceof IRScriptBody)
        );
    }

    /*
     * Since the return is now going to be preceded by a pops of bindings/frames,
     * the return value should continue to be valid after those pops.
     * If not, introduce a copy into a tmp-var before the pops and use the tmp-var
     * to return the right value.
     */
    private void fixReturn(FullInterpreterContext fic, ReturnBase i, ListIterator<Instr> instrs) {
        Operand retVal = i.getReturnValue();
        if (!(retVal instanceof ImmutableLiteral || retVal instanceof TemporaryVariable)) {
            TemporaryVariable tmp = fic.createTemporaryVariable();
            CopyInstr copy = new CopyInstr(tmp, retVal);
            i.updateReturnValue(tmp);
            instrs.previous();
            instrs.add(copy);
            instrs.next();
        }
    }

    private void popSavedState(IRScope scope, boolean isGEB, boolean requireBinding, boolean requireFrame, Variable savedViz, Variable savedFrame, ListIterator<Instr> instrs) {
        if (scope instanceof IRClosure && isGEB) {
            // Add before RethrowSavedExcInLambdaInstr
            instrs.previous();
        }
        if (requireBinding) instrs.add(new PopBindingInstr());
        if (scope instanceof IRClosure) {
            if (scope.needsFrame()) {
                instrs.add(new RestoreBindingVisibilityInstr(savedViz));
                instrs.add(new PopBlockFrameInstr(savedFrame));
            }
        } else {
            if (requireFrame) {
                if (scope.needsOnlyBackref()) {
                    instrs.add(new PopBackrefFrameInstr());
                } else {
                    instrs.add(new PopMethodFrameInstr());
                }
            }
        }
    }

    @Override
    public Object execute(FullInterpreterContext fic, Object... data) {
        // IRScriptBody do not get explicit call protocol instructions right now.
        // They dont push/pop a frame and do other special things like run begin/end blocks.
        // So, for now, they go through the runtime stub in IRScriptBody.
        //
        // Add explicit frame and binding push/pop instrs ONLY for methods -- we cannot handle this in closures and evals yet
        // If the scope uses $_ or $~ family of vars, has local load/stores, or if its binding has escaped, we have
        // to allocate a dynamic scope for it and add binding push/pop instructions.
        if (!explicitCallProtocolSupported(fic.getScope())) return null;

        fic.getFlags().remove(IRFlags.FLAGS_COMPUTED);
        fic.getScope().computeScopeFlags();

        CFG cfg = fic.getCFG();
        IRScope scope = fic.getScope();

        // For now, we always require frame for closures
        boolean requireFrame = scope.needsFrame();
        boolean requireBinding = fic.needsBinding();

        if (fic.getScope() instanceof IRClosure || requireBinding || requireFrame) {
            BasicBlock entryBB = cfg.getEntryBB();
            Variable savedViz = null, savedFrame = null;
            if (fic.getScope() instanceof IRClosure) {
                savedViz = fic.createTemporaryVariable();
                savedFrame = fic.createTemporaryVariable();

                // FIXME: Hacky...need these to come before other stuff in entryBB so we insert instead of add
                int insertIndex = 0;

                if (requireFrame) {
                    entryBB.insertInstr(insertIndex++, new SaveBindingVisibilityInstr(savedViz));
                    entryBB.insertInstr(insertIndex++, new PushBlockFrameInstr(savedFrame, fic.getName()));
                }

                // NOTE: Order of these next two is important, since UBESI resets state PBBI needs.
                if (requireBinding) {
                    entryBB.insertInstr(insertIndex++, new PushBlockBindingInstr());
                }

                entryBB.insertInstr(insertIndex++, new UpdateBlockExecutionStateInstr(Self.SELF));

                BasicBlock prologueBB = createPrologueBlock(cfg);

                // Add the right kind of arg preparation instruction
                Signature sig = ((IRClosure)fic.getScope()).getSignature();
                int arityValue = sig.arityValue();
                if (arityValue == 0) {
                    prologueBB.addInstr(PrepareNoBlockArgsInstr.INSTANCE);
                } else {
                    if (sig.isFixed()) {
                        if (arityValue == 1) {
                            prologueBB.addInstr(PrepareSingleBlockArgInstr.INSTANCE);
                        } else {
                            prologueBB.addInstr(PrepareBlockArgsInstr.INSTANCE);
                        }
                    } else {
                        prologueBB.addInstr(PrepareBlockArgsInstr.INSTANCE);
                    }
                }
            } else {
                if (requireFrame) {
                    if (scope.needsOnlyBackref()) {
                        entryBB.addInstr(new PushBackrefFrameInstr());
                    } else {
                        entryBB.addInstr(new PushMethodFrameInstr(
                                fic.getName(),
                                fic.getScope().isScriptScope() ? Visibility.PRIVATE : Visibility.PUBLIC));
                    }
                }
                if (requireBinding) entryBB.addInstr(new PushMethodBindingInstr());
            }

            // SSS FIXME: We are doing this conservatively.
            // Only scopes that have unrescued exceptions need a GEB.
            //
            // Allocate GEB if necessary for popping
            BasicBlock geb = cfg.getGlobalEnsureBB();
            boolean gebProcessed = false;
            if (geb == null) {
                Variable exc = fic.createTemporaryVariable();
                geb = new BasicBlock(cfg, Label.getGlobalEnsureBlockLabel());
                geb.addInstr(new ReceiveJRubyExceptionInstr(exc)); // JRuby Implementation exception handling
                geb.addInstr(new ThrowExceptionInstr(exc));
                cfg.addGlobalEnsureBB(geb);
            }

            // Pop on all scope-exit paths
            for (BasicBlock bb: cfg.getBasicBlocks()) {
                Instr i = null;
                ListIterator<Instr> instrs = bb.getInstrs().listIterator();
                while (instrs.hasNext()) {
                    i = instrs.next();
                    // Breaks & non-local returns in blocks will throw exceptions
                    // and pops for them will be handled in the GEB
                    if (!bb.isExitBB() && i instanceof ReturnInstr) {
                        // Frame holds backref and lastline, binding holds heap scopes
                        if (requireFrame || requireBinding) fixReturn(fic, (ReturnInstr)i, instrs);
                        // Add before the break/return
                        i = instrs.previous();
                        popSavedState(scope, bb == geb, requireBinding, requireFrame, savedViz, savedFrame, instrs);
                        if (bb == geb) gebProcessed = true;
                        break;
                    }
                }

                if (bb.isExitBB() && !bb.isEmpty()) {
                    // Last instr could be a return -- so, move iterator one position back
                    if (i != null && i instanceof ReturnInstr) {
                        // Frame holds backref and lastline, binding holds heap scopes
                        if (requireFrame || requireBinding) fixReturn(fic, (ReturnInstr)i, instrs);
                        instrs.previous();
                    }
                    popSavedState(scope, bb == geb, requireBinding, requireFrame, savedViz, savedFrame, instrs);
                    if (bb == geb) gebProcessed = true;
                } else if (!gebProcessed && bb == geb) {
                    // Add before throw-exception-instr which would be the last instr
                    if (i != null) {
                        // Assumption: Last instr should always be a control-transfer instruction
                        assert i.getOperation().transfersControl(): "Last instruction of GEB in scope: " + scope + " is " + i + ", not a control-xfer instruction";
                        instrs.previous();
                    }
                    popSavedState(scope, true, requireBinding, requireFrame, savedViz, savedFrame, instrs);
                }
            }
        }

/*
        if (scope instanceof IRClosure) {
            System.out.println(scope + " after acp: " + cfg.toStringInstrs());
        }
*/

        // This scope has an explicit call protocol flag now
        fic.setExplicitCallProtocol(true);

        // LVA information is no longer valid after the pass
        // FIXME: Grrr ... this seems broken to have to create a new object to invalidate
        (new LiveVariableAnalysis()).invalidate(fic);

        return null;
    }

    // We create an extra BB after entryBB for some ACP instructions which can possibly throw
    // an exception.  We want to keep them out of entryBB so we have a safe place to put
    // stuff before exception without needing to worry about weird flow control.
    // FIXME: We need to centralize prologue logic in case there's other places we want to use it
    private BasicBlock createPrologueBlock(CFG cfg) {
        BasicBlock entryBB = cfg.getEntryBB();

        BasicBlock oldStart = cfg.getOutgoingDestinationOfType(entryBB, CFG.EdgeType.FALL_THROUGH);
        BasicBlock prologueBB = new BasicBlock(cfg, cfg.getScope().getNewLabel());
        cfg.removeEdge(entryBB, oldStart);
        cfg.addBasicBlock(prologueBB);
        cfg.addEdge(entryBB, prologueBB, CFG.EdgeType.FALL_THROUGH);
        cfg.addEdge(prologueBB, oldStart, CFG.EdgeType.FALL_THROUGH);

        // If there's already a GEB, make sure we have an edge to it and use it to rescue these instrs
        if (cfg.getGlobalEnsureBB() != null) {
            BasicBlock geb = cfg.getGlobalEnsureBB();
            cfg.addEdge(prologueBB, geb, CFG.EdgeType.EXCEPTION);
            cfg.setRescuerBB(prologueBB, geb);
        }

        return prologueBB;
    }

    @Override
    public boolean invalidate(FullInterpreterContext fic) {
        // Cannot add call protocol instructions after we've added them once.
        return false;
    }
}
