package edu.mit.compilers.codegen;

import java.util.Collection;

public interface ControlFlowNode {

    /**
     * Get all the ControlFlowNodes that this node can flow to.
     * 
     * This method will probably only be used for printing Control Flow Graphs,
     * because we will need more specific getters to actually do meaningful
     * computation.
     */
    public Collection<ControlFlowNode> getSinks();
}
