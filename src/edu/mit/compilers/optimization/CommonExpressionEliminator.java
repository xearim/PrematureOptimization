package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlowUtil;
import edu.mit.compilers.common.Variable;

/**
 * Preforms CSE (common subexpression elimination).
 *
 * <p>Currently, subexpressions aren't eliminated-- only full expressions are eliminated.
 * That is, if (x + y) is available, we will not (yet!) optimize (1 + x + y).
 *
 * <p>This should really be a non-instantiable class with a static optimize method, but
 * it needs to be instantiable to conform to DataFlowOptimizer's interface.
 */
public class CommonExpressionEliminator implements DataFlowOptimizer {

    private static final String TEMP_VAR_PREFIX = "cse_temp";

    @Override
    public void optimize(DataFlowNode head) {
        new Eliminator(head).optimize();
    }

    // TODO(jasonpr): Figure out why this helper class feels so hacky, and unhack it.
    /**
     * Helper class for actually performing the optimization.
     *
     * <p>It allows us to store the AvailabilityCalculator and some other
     * values in instance variables, so we don't need to keep passing them
     * around as parameters.
     *
     */
    private static final class Eliminator {
        private final DataFlowNode head;
        private final AvailabilityCalculator availCalc;
        private final Collection<DataFlowNode> nodes;
        private final Map<GeneralExpression, Variable> tempVars;

        public Eliminator(DataFlowNode head) {
            this.head = head;
            this.availCalc = new AvailabilityCalculator(head);
            this.nodes = DataFlowUtil.reachableFrom(head);
            this.tempVars = tempVars(expressions(nodes));
        }

        public void optimize() {
            for (DataFlowNode node : DataFlowUtil.reachableFrom(head)) {
                for (GeneralExpression expr : nodeExprs(node)) {
                    if (availCalc.isAvailable(expr, node)) {
                        replace(node, useTemp(node, expr));
                    } else {
                        // For now, we alway generate if it's not available.
                        // TODO(jasonpr): Use 'reasons' or 'benefactors' to reduce
                        // amount of unnecessary temp filling.
                        replace(node, fillAndUseTemp(node, expr));
                    }
                }
            }
        }


        /**
         * Return a replacement for 'node' that does not call for 'expr' to be
         * evaluated, but instead loads its value from the corresponding temp
         * variable.
         *
         * <p>Requires that the expression is available at the node.
         */
        private DataFlowNode useTemp(DataFlowNode node, GeneralExpression expr) {
            // TODO(jasonpr): Implement!
            throw new RuntimeException("Not yet implemented!");
        }

        /**
         * Return a replacement for 'node' that stores the value of 'expr' in
         * its temp variable, and uses that temp variable when executing the
         * node's statement.
         */
        private DataFlowNode fillAndUseTemp(DataFlowNode node, GeneralExpression expr) {
            // TODO(jasonpr): Implement!
            throw new RuntimeException("Not yet implemented!");
        }
    }

    /** Generate a map from expression to temporary variable, for some expressions. */
    private static Map<GeneralExpression, Variable> tempVars(Iterable<GeneralExpression> exprs) {
        ImmutableMap.Builder<GeneralExpression, Variable> builder = ImmutableMap.builder();
        int tempNumber = 0;
        for (GeneralExpression expr : exprs) {
            builder.put(expr, Variable.forCompiler(TEMP_VAR_PREFIX + tempNumber++));
        }
        return builder.build();
    }

    /** Get all the optimizable expressions from some nodes. */
    private static Iterable<GeneralExpression> expressions(Iterable<DataFlowNode> nodes) {
        ImmutableSet.Builder<GeneralExpression> builder = ImmutableSet.builder();
        for (DataFlowNode node : nodes) {
            builder.addAll(nodeExprs(node));
        }
        return builder.build();
    }

    /**
     * Get all the expressions in the node to try to optimize.
     *
     * For now, we ONLY optimize the top-level expressions-- no subexpressions!
     */
    private static Collection<GeneralExpression> nodeExprs(DataFlowNode node) {
        return node.getExpressions();
    }

    /**
     * Replace a data flow node.
     *
     * <p>The replacement will be hooked up to the old node's predecessor and successor.
     */
    private static void replace(DataFlowNode old, DataFlowNode replacement) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }
}
