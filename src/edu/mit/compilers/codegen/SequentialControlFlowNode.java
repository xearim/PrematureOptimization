package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.instructions.Instruction;


public class SequentialControlFlowNode implements ControlFlowNode {

    private final Optional<Instruction> instruction;
    private final Optional<ControlFlowNode> next;

    // TODO(manny): Figure out if it's feasible to pass "next" as a parameter.
    // It would force us to build up sequences of nodes "backwards", which may
    // or may not be ugly.
    private SequentialControlFlowNode(Optional<Instruction> instruction,
            Optional<ControlFlowNode> next) {
        this.instruction = instruction;
        this.next = next;
    }

    private SequentialControlFlowNode(Optional<ControlFlowNode> next) {
        this(Optional.<Instruction>absent(), next);
    }

    public static SequentialControlFlowNode nopWithNext(ControlFlowNode next) {
        return new SequentialControlFlowNode(Optional.of(next));
    }

    public static SequentialControlFlowNode nopTerminal() {
        return new SequentialControlFlowNode(Optional.<ControlFlowNode>absent());
    }

    public static SequentialControlFlowNode
            WithNext(Instruction instruction, ControlFlowNode next) {
        return new SequentialControlFlowNode(Optional.of(instruction), Optional.of(next));
    }

    public static SequentialControlFlowNode terminal(Instruction instruction) {
        return new SequentialControlFlowNode(Optional.of(instruction),
                Optional.<ControlFlowNode>absent());
    }

    public boolean hasNext() {
        return next.isPresent();
    }

    /**
     * Gets the next node.
     *
     * <p>Throws IllegalStateExcpetion if there is no next node.
     */
    public ControlFlowNode getNext() {
        return next.get();
    }

    public boolean hasInstruction() {
        return instruction.isPresent();
    }

    public Instruction getInstruction() {
        return instruction.get();
    }

    @Override
    public Collection<ControlFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<ControlFlowNode>of(getNext())
                : ImmutableList.<ControlFlowNode>of();
    }
}
