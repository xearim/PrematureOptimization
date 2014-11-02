package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.codegen.DataFlowNode;

/**
 * Finds the availablility of expressions in a data flow graph. 
 *
 * <p>This is just a wrapper around an as-yet-nonexistant workhorse class that
 * actually does this work.  It will probably be eliminated once the workhorse
 * class is created.
 */
public class AvailabilityFinder {

    /** Computes the available expressions for all DataFlowNodes reachable from 'head'. */
    public AvailabilityFinder(DataFlowNode head) {
        // TODO(jasonpr): Hook in Manny's code.
    }
    
    /** Gets the set of expressions available at a DataFlowNode. */
    public Set<GeneralExpression> availableAt(DataFlowNode node) {
        // TODO(jasonpr): Hook in Manny's code.
        throw new RuntimeException("Not yet implemented.");
    }
}
