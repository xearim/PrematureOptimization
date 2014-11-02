package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.codegen.DataFlowNode;

public interface DataFlowOptimizer {

    /**
     * Make an optimized data flow graph for the graph that starts at 'head'.
     *
     * <p>The optimization is performed in place.  That is, the data flow graph is mutated.
     *
     * @param head The head of the data flow graph to optimize.
     */
    public void optimize(DataFlowNode head);
}
