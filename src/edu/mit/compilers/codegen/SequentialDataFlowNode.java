package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * A node in a Data Flow Graph that has one predecessor and one successor.
 */
public abstract class SequentialDataFlowNode implements DataFlowNode{

    private Optional<DataFlowNode> prev, next;
    private final String name;

    protected SequentialDataFlowNode(Optional<DataFlowNode> prev, Optional<DataFlowNode> next, String name) {
        super();
        this.prev = prev;
        this.next = next;
        this.name = name;
    }

    protected SequentialDataFlowNode(String name) {
        this(Optional.<DataFlowNode>absent(), Optional.<DataFlowNode>absent(), name);
    }

    public static void link(SequentialDataFlowNode prev, SequentialDataFlowNode next) {
        prev.setNext(next);
        next.setPrev(prev);
    }

    public static void link(SequentialDataFlowNode prev, BranchSinkDataFlowNode next) {
        prev.setNext(next);
        next.setPrev(prev);
    }

    public static void link(SequentialDataFlowNode prev, BranchSourceDataFlowNode next) {
        prev.setNext(next);
        next.setPrev(prev);
    }

    public static void link(BranchSinkDataFlowNode prev, SequentialDataFlowNode next) {
        prev.setNext(next);
        next.setPrev(prev);
    }

    public boolean hasNext() {
        return next.isPresent();
    }

    public boolean hasPrev() {
        return prev.isPresent();
    }

    public DataFlowNode getNext() {
        return next.get();
    }

    public DataFlowNode getPrev() {
        return prev.get();
    }

    public void setNext(DataFlowNode next) {
        Preconditions.checkState(!this.equals(next));
        this.next = Optional.of(next);
    }

    public void setPrev(DataFlowNode prev) {
        Preconditions.checkState(!this.equals(prev));
        this.prev = Optional.of(prev);
    }

    public void clearNext() {
        this.next = Optional.absent();
    }

    public void clearPrev() {
        this.prev = Optional.absent();
    }

    public Set<DataFlowNode> getPredecessors() {
        return (hasPrev())
                ? ImmutableSet.<DataFlowNode>of(getPrev())
                        : ImmutableSet.<DataFlowNode>of();
    }

    public Set<DataFlowNode> getSuccessors() {
        return (hasNext())
                ? ImmutableSet.<DataFlowNode>of(getNext())
                : ImmutableSet.<DataFlowNode>of();
    }

    @Override
    public void replacePredecessor(DataFlowNode replaced,
            DataFlowNode replacement) {
        checkArgument(replaced.equals(prev.get()));
        prev = Optional.of(replacement);
    }

    @Override
    public void replaceSuccessor(DataFlowNode replaced, DataFlowNode replacement) {
        checkArgument(replaced.equals(next.get()));
        next = Optional.of(replacement);
    }
}
