package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Condition;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

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
        return new Eliminator(ir).optimized();
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
        private final BcrFlowGraph<ScopedStatement> dataFlowGraph;
        // TODO(jasonpr): Use ScopedExpression, not NativeExpression.
        private final Map<NativeExpression, Variable> tempVars;
        private final Multimap<Node<ScopedStatement>, ScopedExpression> inSets;

        public Eliminator(DataFlowIntRep ir) {
            this.ir = ir;
            this.dataFlowGraph = ir.getDataFlowGraph();
            this.tempVars = tempVars(expressions(dataFlowGraph));
            inSets = DataFlowAnalyzer.AVAILABLE_EXPRESSIONS.calculateAvailability(ir.getDataFlowGraph());
        }

        public DataFlowIntRep optimized() {
            BcrFlowGraph.Builder<ScopedStatement> newBuilder =
                    BcrFlowGraph.builderOf(dataFlowGraph);
            Scope newScope = new Scope(ir.getScope());

            for (Node<ScopedStatement> node : dataFlowGraph.getNodes()) {
                if (!node.hasValue()) {
                    // NOPs have nothing to optimize!
                    continue;
                }

                for (NativeExpression expr : nodeExprs(node.value())) {
                    if (!isComplexEnough(expr)) {
                        continue;
                    }

                    if (isAvailable(expr, node)) {
                        newBuilder.replace(node, useTemp(node.value(), expr));
                    } else {
                        // For now, we alway generate if it's not available.
                        if (node.value().getStatement() instanceof MethodCall) {
                            // Just skip it!  We only call it for its side effects.
                            continue;
                        }
                        addTempToScope(node.value(), expr, newScope);
                        newBuilder.replace(node, fillAndUseTemp(node.value(), expr));
                    }
                }
            }

            return new DataFlowIntRep(newBuilder.build(), newScope);
        }

        /** Return whether an expression is available at a DataFlowNode. */
        private boolean isAvailable(GeneralExpression expr, Node<ScopedStatement> node) {
            if (!(expr instanceof NativeExpression)) {
                // Only NativeExpressions are ever available.
                return false;
            }

            if (!node.hasValue()) {
                return false;
            }

            ScopedExpression scopedExpr = new ScopedExpression((NativeExpression) expr, node.value().getScope());

            // TODO(xearim): Figure out why a direct contains() call doesn't work.
            for(ScopedExpression ex : inSets.get(node)){
                if(ex.equals(scopedExpr)){
                    return true;
                }
            }
            return false;
        }


        /**
         * Return a replacement for 'node' that does not call for 'expr' to be
         * evaluated, but instead loads its value from the corresponding temp
         * variable.
         *
         * <p>Requires that the expression is available at the node.
         */
        private FlowGraph<ScopedStatement> useTemp(ScopedStatement node, GeneralExpression expr) {
            Preconditions.checkState(tempVars.containsKey(expr));
            Preconditions.checkState(node.getStatement().hasExpression());

            Location temp = new ScalarLocation(tempVars.get(expr), LocationDescriptor.machineCode());
            StaticStatement newStatement = getReplacement(node.getStatement(), temp);

            return BasicFlowGraph.<ScopedStatement>builder()
                    .append(new ScopedStatement(newStatement, node.getScope()))
                    .build();
        }

        /**
         * Return a replacement for 'node' that stores the value of 'expr' in
         * its temp variable, and uses that temp variable when executing the
         * node's statement.
         */

        private FlowGraph<ScopedStatement> fillAndUseTemp(ScopedStatement node, NativeExpression expr) {
            // The node to replace should actually contain statements
            Preconditions.checkState(tempVars.containsKey(expr));
            Preconditions.checkState(node.getStatement().hasExpression());

            Location temp = new ScalarLocation(tempVars.get(expr), LocationDescriptor.machineCode());

            Assignment newTemp = Assignment.compilerAssignment(temp, expr);
            StaticStatement newStatement = getReplacement(node.getStatement(), temp);

            return BasicFlowGraph.<ScopedStatement>builder()
                    .append(new ScopedStatement(newTemp, node.getScope()))
                    .append(new ScopedStatement(newStatement, node.getScope()))
                    .build();
        }

        private StaticStatement getReplacement(StaticStatement statement, NativeExpression replacement) {
            if(statement instanceof Assignment){
                return Assignment.assignmentWithReplacementExpr((Assignment) statement, replacement);
            } else if(statement instanceof Condition){
                return new Condition(replacement);
            } else if(statement instanceof MethodCall){
                throw new AssertionError("Right now we cannot replace methods, we dont know if they are idempotent");
            } else if(statement instanceof ReturnStatement){
                return ReturnStatement.compilerReturn(replacement);
            } else {
                throw new AssertionError("Unexpected StaticStatement type for " + statement);
            }
        }

        private void addTempToScope(ScopedStatement scopedStatement, NativeExpression expr, Scope scope) {
            Variable var = tempVars.get(expr);
            FieldDescriptor fieldDesc = FieldDescriptor.forCompilerVariable(var);
            // TODO(jasonpr): Add to most specific scope possible.
            scope.addVariable(fieldDesc);
        }
    }

    /**
     * Generate a map from expression to temporary variable, for some expressions.
     *
     * <p>Non-native expressions are ignored-- we can't store them in variables!
     */
    private static Map<NativeExpression, Variable> tempVars(Iterable<NativeExpression> exprs) {
        ImmutableMap.Builder<NativeExpression, Variable> builder = ImmutableMap.builder();
        int tempNumber = 0;
        for (NativeExpression expr : exprs) {
            builder.put(expr, Variable.forCompiler(TEMP_VAR_PREFIX + tempNumber++));
        }
        return builder.build();
    }

    /** Get all the optimizable expressions from some nodes. */
    private static Iterable<NativeExpression> expressions(FlowGraph<ScopedStatement> dataFlowGraph) {
        ImmutableSet.Builder<NativeExpression> builder = ImmutableSet.builder();
        for (Node<ScopedStatement> node : dataFlowGraph.getNodes()) {
            if (node.hasValue()) {
                builder.addAll(nodeExprs(node.value()));
            }
        }
        return builder.build();
    }

    /**
     * Get all the expressions in the node to try to optimize.
     *
     * <p>For now, we ONLY optimize the top-level expressions-- no subexpressions!
     */
    private static Collection<NativeExpression> nodeExprs(ScopedStatement scopedStatement) {
        StaticStatement statement = scopedStatement.getStatement();
        return statement.hasExpression()
                ? ImmutableList.of(statement.getExpression())
                : ImmutableList.<NativeExpression>of();
    }

    /**
     * Determines if a NativeExpression is complex enough to be worth saving.
     *
     * <p>Does not check for MethodCalls inside of GeneralExpression.
     *
     * <p>Any GeneralExpression that passes this check may be considered a
     * NativeExpression.
     */
    private static boolean isComplexEnough(GeneralExpression ge) {
        return (ge instanceof BinaryOperation)
                || (ge instanceof MethodCall)
                || (ge instanceof TernaryOperation)
                || (ge instanceof UnaryOperation);
    }
}
