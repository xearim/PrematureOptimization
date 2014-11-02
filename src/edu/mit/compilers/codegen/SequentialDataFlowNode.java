package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class SequentialDataFlowNode extends DataFlowNode{

    private Optional<DataFlowNode> prev, next;
    private final String name;

    private SequentialDataFlowNode(Optional<DataFlowNode> prev, Optional<DataFlowNode> next, String name) {
        super();
        this.prev = prev;
        this.next = next;
        this.name = name;
    }

    public SequentialDataFlowNode(String name) {
        this(Optional.<DataFlowNode>absent(), Optional.<DataFlowNode>absent(), name);
    }

    public static SequentialDataFlowNode nop() {
        return new SequentialDataFlowNode("");
    }

    public static SequentialDataFlowNode nopNamed(String name) {
        return new SequentialDataFlowNode(name);
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

    @Override
    public Collection<DataFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<DataFlowNode>of(getNext())
                        : ImmutableList.<DataFlowNode>of();
    }

    @Override
    public String nodeText() {
        return name;
    }

    public Set<DataFlowNode> getPredecessors() {
        return (hasPrev())
                ? ImmutableSet.<DataFlowNode>of(getNext())
                        : ImmutableSet.<DataFlowNode>of();
    }

    public Set<DataFlowNode> getSuccessors() {
        return (hasNext())
                ? ImmutableSet.<DataFlowNode>of(getPrev())
                : ImmutableSet.<DataFlowNode>of();
    }
}
