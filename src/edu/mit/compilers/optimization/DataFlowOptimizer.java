package edu.mit.compilers.optimization;

import edu.mit.compilers.codegen.DataFlowIntRep;

public interface DataFlowOptimizer {

    /** Return an optimized DataFlowIntRep with the same semantics as the original. */
    public DataFlowIntRep optimized(DataFlowIntRep ir);
}
