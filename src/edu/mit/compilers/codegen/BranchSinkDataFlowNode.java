package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;

public class BranchSinkDataFlowNode implements DataFlowNode{

    private String name;
    private Set<DataFlowNode> prev;
    private Optional<DataFlowNode> next;

    public BranchSinkDataFlowNode(String name){
        super();
        this.name = name;
        this.prev = new HashSet<DataFlowNode>();
        this.next = Optional.<DataFlowNode>absent();
    }

    public BranchSinkDataFlowNode(){
        this("sink");
    }

    public boolean hasNext() {
        return next.isPresent();
    }

    public boolean hasPrev() {
        return !prev.isEmpty();
    }

    public boolean prevContains(DataFlowNode node){
        return prev.contains(node);
    }

    public DataFlowNode getNext() {
        return next.get();
    }

    public Collection<DataFlowNode> getPrev() {
        return prev;
    }

    public void setNext(DataFlowNode next) {
        Preconditions.checkState(!this.equals(next));
        this.next = Optional.of(next);
    }

    public void setPrev(DataFlowNode prev) {
        Preconditions.checkState(!this.equals(prev));
        this.prev.add(prev);
    }

    public void clearNext() {
        this.next = Optional.absent();
    }

    public void removeFromPrev(DataFlowNode prev) {
        Preconditions.checkState(this.prev.contains(prev));
        this.prev.remove(prev);
    }

    public void clearPrev() {
        this.prev.clear();
    }


    public Set<DataFlowNode> getPredecessors() {
        return new HashSet<DataFlowNode>(prev);
    }

    public Set<DataFlowNode> getSuccessors() {
        return (hasNext())
                ? ImmutableSet.<DataFlowNode>of(getNext())
                : ImmutableSet.<DataFlowNode>of();
    }

    @Override
    public Collection<GeneralExpression> getExpressions() {
        return ImmutableList.of();
    }
}
