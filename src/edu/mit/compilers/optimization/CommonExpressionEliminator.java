package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow;
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
    public DataFlowIntRep optimized(DataFlowIntRep ir) {
        return new Eliminator(ir).optimize();
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
        private final DataFlowIntRep ir;
        private final AvailabilityCalculator availCalc;
        private final Collection<DataFlowNode> nodes;
        private final Map<GeneralExpression, Variable> tempVars;

        public Eliminator(DataFlowIntRep ir) {
            this.ir = ir;
            this.availCalc = new AvailabilityCalculator(ir.getDataFlowGraph().getBeginning());
            this.nodes = DataFlowUtil.nodesIn(ir);
            this.tempVars = tempVars(expressions(nodes));
        }

        public DataFlowIntRep optimize() {
            for (DataFlowNode node : nodes) {
                // The cast will always succeed: non-statement nodes have no
                // expressions, so we'll never enter this loop if the cast would fail.
                StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
                for (GeneralExpression expr : nodeExprs(node)) {
                    if (availCalc.isAvailable(expr, statementNode)) {
                        replace(statementNode, useTemp(node, expr));
                    } else {
                        // For now, we alway generate if it's not available.
                        // TODO(jasonpr): Use 'reasons' or 'benefactors' to reduce
                        // amount of unnecessary temp filling.
                        replace(statementNode, fillAndUseTemp(node, expr));
                    }
                }
            }
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Must return newly constructed DataFlowIntRep.");
        }


        /**
         * Return a replacement for 'node' that does not call for 'expr' to be
         * evaluated, but instead loads its value from the corresponding temp
         * variable.
         *
         * <p>Requires that the expression is available at the node.
         */
        private DataFlow useTemp(DataFlowNode node, GeneralExpression expr) {
            // TODO(jasonpr): Implement!
            throw new RuntimeException("Not yet implemented!");
        }

        /**
         * Return a replacement for 'node' that stores the value of 'expr' in
         * its temp variable, and uses that temp variable when executing the
         * node's statement.
         */
        private DataFlow fillAndUseTemp(DataFlowNode node, GeneralExpression expr) {
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
    private static void replace(SequentialDataFlowNode old, DataFlow replacement) {
        old.getPrev().replaceSuccessor(old, replacement.getBeginning());
        replacement.getBeginning().setPrev(old.getPrev());

        old.getNext().replacePredecessor(old, replacement.getEnd());
        replacement.getEnd().setNext(old.getNext());

        // This is buggy: the graph we're operating on is part of a DataFlow.
        // If we replace a terminal node, we need that DataFlow to hear about it!
        // So, DataFlowOptimizers must operate on DataFlows, and the terminals must
        // be updated in the correct circumstances.
        // TODO(jasonpr): Fix the bug!
        throw new RuntimeException("Fix the bug!");
    }
}
