package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class BranchSourceDataFlowNode extends DataFlowNode{

    private String name;
    private JumpType type;
    private Optional<SequentialDataFlowNode> prev;
    private Optional<SequentialDataFlowNode> trueBranch, falseBranch;

    public BranchSourceDataFlowNode(JumpType type, String name){
        super();
        this.type = type;
        this.prev = Optional.<SequentialDataFlowNode>absent();
        this.trueBranch = Optional.<SequentialDataFlowNode>absent();
        this.falseBranch = Optional.<SequentialDataFlowNode>absent();
    }

    public BranchSourceDataFlowNode(JumpType type){
        this(type, "branch");
    }

    public boolean hasNext() {
        return trueBranch.isPresent() && falseBranch.isPresent();
    }

    public boolean hasPrev() {
        return prev.isPresent();
    }

    public JumpType getType() {
        return type;
    }

    public SequentialDataFlowNode getTrueBranch() {
        return trueBranch.get();
    }

    public SequentialDataFlowNode getFalseBranch() {
        return falseBranch.get();
    }

    public SequentialDataFlowNode getPrev() {
        return prev.get();
    }

    public void setTrueBranch(SequentialDataFlowNode trueBranch) {
        Preconditions.checkState(!this.equals(trueBranch));
        this.trueBranch = Optional.of(trueBranch);
    }

    public void setFalseBranch(SequentialDataFlowNode falseBranch) {
        Preconditions.checkState(!this.equals(trueBranch));
        this.trueBranch = Optional.of(falseBranch);
    }

    public void setPrev(SequentialDataFlowNode prev) {
        Preconditions.checkState(!this.equals(prev));
        this.prev = Optional.of(prev);
    }

    public void clearTrueBranch() {
        this.trueBranch = Optional.absent();
    }

    public void clearFalseBranch() {
        this.falseBranch = Optional.absent();
    }

    public void clearPrev() {
        this.prev = Optional.absent();
    }

    @Override
    public Collection<DataFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<DataFlowNode>of(getTrueBranch(), getFalseBranch())
                        : ImmutableList.<DataFlowNode>of();
    }

    @Override
    public String nodeText() {
        return name;
    }

    public Set<DataFlowNode> getPredecessors() {
        return (hasPrev())
                ? ImmutableSet.<DataFlowNode>of(getPrev())
                : ImmutableSet.<DataFlowNode>of();
    }

    public Set<DataFlowNode> getSuccessors() {
        return (hasNext())
                ? ImmutableSet.<DataFlowNode>of(getTrueBranch(), getFalseBranch())
                : ImmutableSet.<DataFlowNode>of();
    }

    @Override
    public Collection<GeneralExpression> getExpressions() {
        return ImmutableList.of();
    }
}
