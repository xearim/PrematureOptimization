package edu.mit.compilers.codegen;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

/**
 * Represents a conditional in the control flow graph. Will always use a Jcc
 * assembly instruction
 *
 * TODO(jasonpr): Determine exactly when the {true,false} condition is followed.
 */
public class BranchingControlFlowNode implements ControlFlowNode {
    private final JumpType type;
    private final ControlFlowNode trueBranch;
    private final ControlFlowNode falseBranch;

    public BranchingControlFlowNode(JumpType type, ControlFlowNode trueBranch,
            ControlFlowNode falseBranch) {
        // TODO(Manny): find more meaningful error message
        Preconditions.checkNotNull(trueBranch, "True Branch is null.");
        Preconditions.checkNotNull(falseBranch, "False Branch is null.");

        this.type = type;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public JumpType getType() {
        return type;
    }

    public ControlFlowNode getTrueBranch() {
        return trueBranch;
    }

    public ControlFlowNode getFalseBranch() {
        return falseBranch;
    }

    @Override
    public List<ControlFlowNode> getSinks() {
        return ImmutableList.of(trueBranch,falseBranch);
    }

    // TODO(manny): getters and getSinks
}
