package edu.mit.compilers.optimization;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.DataFlowIntRep;

public interface DataFlowOptimizer {

    /** Return an optimized copy of the IR. */
    public DataFlowIntRep optimized(DataFlowIntRep ir);
}
