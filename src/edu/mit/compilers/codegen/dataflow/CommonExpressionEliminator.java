package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.codegen.DataFlowNode;

/**
 * Preforms CSE (common subexpression eliminator).
 *
 * <p>Currently, subexpressions aren't eliminated-- only full expressions are eliminated.
 * That is, if (x + y) is available, we will not (yet!) optimize (1 + x + y).
 *
 * This should really be a non-instantiable class with a static optimize method, but
 * it needs to be instantiable to conform to DataFlowOptimizer's interface.
 */
public class CommonExpressionEliminator implements DataFlowOptimizer {

    @Override
    public void optimize(DataFlowNode head) {
        // TODO(jasonpr): Implement!
    }
}
