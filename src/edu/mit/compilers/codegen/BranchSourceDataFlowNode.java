package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class BranchSourceDataFlowNode implements DataFlowNode{

    private String name;
    private JumpType type;
    private Optional<DataFlowNode> prev;
    private Optional<DataFlowNode> trueBranch, falseBranch;

    public BranchSourceDataFlowNode(JumpType type, String name){
        super();
        this.type = type;
        this.prev = Optional.<DataFlowNode>absent();
        this.trueBranch = Optional.<DataFlowNode>absent();
        this.falseBranch = Optional.<DataFlowNode>absent();
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

    public DataFlowNode getTrueBranch() {
        return trueBranch.get();
    }

    public DataFlowNode getFalseBranch() {
        return falseBranch.get();
    }

    public DataFlowNode getPrev() {
        return prev.get();
    }

    public void setTrueBranch(DataFlowNode trueBranch) {
        Preconditions.checkState(!this.equals(trueBranch));
        this.trueBranch = Optional.of(trueBranch);
    }

    public void setFalseBranch(DataFlowNode falseBranch) {
        Preconditions.checkState(!this.equals(trueBranch));
        this.falseBranch = Optional.of(falseBranch);
    }

    public void setPrev(DataFlowNode prev) {
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

    @Override
    public void replacePredecessor(DataFlowNode replaced,
            DataFlowNode replacement) {
        checkArgument(replaced.equals(prev.get()));
        prev = Optional.of(replaced);
    }

    @Override
    public void replaceSuccessor(DataFlowNode replaced, DataFlowNode replacement) {
        if (replaced.equals(trueBranch.get())) {
            trueBranch = Optional.of(replacement);
        } else if (replaced.equals(falseBranch.get())) {
            falseBranch = Optional.of(replacement);
        } else {
            throw new AssertionError("Could not find successor " + replaced);
        }
    }
}
