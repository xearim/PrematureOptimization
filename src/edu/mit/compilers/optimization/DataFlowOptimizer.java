package edu.mit.compilers.optimization;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.DataFlowIntRep;

public interface DataFlowOptimizer {

    // TODO(jasonpr): Figure out a good way to copy a DataFlow, so that we
    // can make a copy of the DataFlowIntRep and not mutate the original.
    /** Apply a data flow optimization to the IR (mutating it). */
    public void optimize(DataFlowIntRep ir);
}
