package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.instructions.Instruction;


public class SequentialControlFlowNode extends ControlFlowNode {

    private final Optional<Instruction> instruction;
    private Optional<ControlFlowNode> next;
    private final String name;

    private SequentialControlFlowNode(Optional<Instruction> instruction,
            Optional<ControlFlowNode> next, String name) {
    	super();
        this.instruction = instruction;
        this.next = next;
        this.name = name;
    }

    private SequentialControlFlowNode(Optional<ControlFlowNode> next, String name) {
        this(Optional.<Instruction>absent(), next, name);
    }

    public static SequentialControlFlowNode nopWithNext(ControlFlowNode next, String name) {
        return new SequentialControlFlowNode(Optional.of(next), name);
    }

    public static SequentialControlFlowNode nopTerminal() {
        return new SequentialControlFlowNode(Optional.<ControlFlowNode>absent(), "");
    }

    public static SequentialControlFlowNode
            WithNext(Instruction instruction, ControlFlowNode next) {
        return new SequentialControlFlowNode(Optional.of(instruction), Optional.of(next), "");
    }

    public static SequentialControlFlowNode terminal(Instruction instruction) {
        return new SequentialControlFlowNode(Optional.of(instruction),
                Optional.<ControlFlowNode>absent(), "");
    }

    public static SequentialControlFlowNode namedNop(String name) {
        return new SequentialControlFlowNode(Optional.<ControlFlowNode>absent(), name);
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
    
    public void setNext(ControlFlowNode next) {
        Preconditions.checkState(!this.equals(next));
        this.next = Optional.of(next);
    }
    
    public void clearNext() {
        this.next = Optional.absent();
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

    @Override
    public String nodeText() {
        return instruction.isPresent()
                ? instruction.get().inAttSyntax()
                : "NOP (" + name + ")";
    }
}
