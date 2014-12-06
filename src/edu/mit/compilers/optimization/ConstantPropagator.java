package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Node;

public class ConstantPropagator implements DataFlowOptimizer {

    @Override
    public DataFlowIntRep optimized(DataFlowIntRep ir) {
        BcrFlowGraph<ScopedStatement> dfg = ir.getDataFlowGraph();

        Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs =
                DataFlowAnalyzer.REACHING_DEFINITIONS.calculate(dfg);

        // We do not modify the scopes at all, so we use the original method scope.
        return new DataFlowIntRep(constantsPropagated(dfg, reachingDefs), ir.getScope());
    }

    private BcrFlowGraph<ScopedStatement> constantsPropagated(
            BcrFlowGraph<ScopedStatement> original,
            Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs) {
        BcrFlowGraph.Builder<ScopedStatement> builder = BcrFlowGraph.builderOf(original);
        for (Node<ScopedStatement> node : original.getNodes()) {
            builder.replace(node, constantsPropagated(node, reachingDefs.get(node)));
        }
        return builder.build();
    }

    private Node<ScopedStatement> constantsPropagated(Node<ScopedStatement> node,
                    Iterable<ReachingDefinition> reachingDefsIterable) {
        if (!node.hasValue() || !node.value().getStatement().hasExpression()) {
            // There's nothing to do if there's no expression.  Return the original node.
            return Node.copyOf(node);
        }
        Multimap<ScopedVariable, Node<ScopedStatement>> reachingDefs =
                Util.reachingDefsMultimap(reachingDefsIterable);

        NativeExpression expr = node.value().getStatement().getExpression();
        for (ScopedVariable var : ScopedVariable.getVariablesOf(node.value())) {
            Optional<NativeLiteral> constant = sameConstant(var, reachingDefs.get(var));
            if (constant.isPresent()) {
                expr = expr.withReplacements(
                        new ScalarLocation(var.getVariable()), constant.get());
            }
        }
        StaticStatement replacement = Util.getReplacement(node.value().getStatement(), expr);
        Scope scope = node.value().getScope();
        return Node.of(new ScopedStatement(replacement, scope));
    }

    private static Optional<NativeLiteral> sameConstant(
            ScopedVariable var, Collection<Node<ScopedStatement>> definitions) {
        if (definitions.isEmpty()) {
            return Optional.absent();
        }

        // Check whether all assignments to the varible are the same constant value.
        NativeLiteral sameConstant = null;
        for (Node<ScopedStatement> scopedStatement : definitions) {
            StaticStatement statement = scopedStatement.value().getStatement();
            if (!isConstantScalarAssignment(statement)) {
                return Optional.absent();
            }
            NativeLiteral constant = getConstantScalarAssignment((Assignment) statement);
            if (sameConstant == null) {
                sameConstant = constant;
            }
            if (!constant.equals(sameConstant)) {
                return Optional.absent();
            }
        }
        return Optional.of(sameConstant);
    }

    private static boolean isConstantScalarAssignment(StaticStatement statement) {
        return (statement instanceof Assignment)
                && (((Assignment) statement).getExpression() instanceof NativeLiteral)
                && (((Assignment) statement).getLocation() instanceof ScalarLocation);
    }

    /**
     * Gets the NativeLiteral that is the RHS of this assignment.
     *
     * <p>Requires that the RHS of this assignment is a native literal!
     */
    private static NativeLiteral getConstantScalarAssignment(Assignment statement) {
        return (NativeLiteral) statement.getExpression(); 
    }
}
