package edu.mit.compilers.codegen;

import java.util.Collection;

/** TODO(jasonpr): Determine exactly when the {true,false} condition is followed. */
public class BranchingControlFlowNode implements ControlFlowNode {
    
    private final ControlFlowNode trueBranch;
    private final ControlFlowNode falseBranch;
    
    /*
     * We're keeping it as a public constructor, because the branches are
     * not terminals themselves.
     */
    public BranchingControlFlowNode(ControlFlowNode trueBranch, ControlFlowNode falseBranch) {
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public Collection<ControlFlowNode> getSinks() {
        // TODO(manny): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
    
    // TODO(manny): getters and getSinks
}
