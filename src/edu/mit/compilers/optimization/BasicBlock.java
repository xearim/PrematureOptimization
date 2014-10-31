package edu.mit.compilers.optimization;

import java.util.List;

public abstract class BasicBlock {
    /**
     * We have chosen to have only one Assignment or MethodCall
     * in each BasicBlock.
     */
    public abstract Subexpression getSubexpression();
    public abstract List<BasicBlock> getPredecessorBlocks();
    public abstract List<BasicBlock> getSuccessorBlocks();
}
