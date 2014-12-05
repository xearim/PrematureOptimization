package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.intersection;
import static edu.mit.compilers.optimization.Util.containsMethodCall;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class AvailabilitySpec implements AnalysisSpec<ScopedStatement, ScopedExpression> {

    /**
     * Get the GEN set for a node, filtering out expressions that contains method calls.
     *
     * <p>Requires that the node has a statement, and that that statement has an expression.
     */
    public Set<ScopedExpression> getGenSet(Node<ScopedStatement> node) {
        if (!node.hasValue() || !node.value().getStatement().hasExpression()) {
            return ImmutableSet.of();
        }
        // Design Decision: don't recurse into method callsf
        NativeExpression ne = node.value().getStatement().getExpression();
        if (containsMethodCall(ne)) {
            /*
             * "b+foo()" can return different values on different calls,
             * so we don't want to claim that "b+foo()" is available.
             */
            return ImmutableSet.of();
        }
        return ImmutableSet.of(new ScopedExpression(ne, node.value().getScope()));
    }

    @Override
    public boolean mustKill(Node<ScopedStatement> curNode, ScopedExpression candidate) {
        Set<ScopedVariable> victimVariables = getPotentiallyChangedVariables(curNode);
        for (ScopedVariable victimVariable : victimVariables) {
            if (candidate.uses(victimVariable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the union of all the sets.
     */
    @Override
    public Set<ScopedExpression> applyConfluenceOperator(Iterable<Collection<ScopedExpression>> outSets) {
        return intersection(outSets);
    }

    /** Filters all the nodes that do not have an expression. */
    @Override 
    public Set<Node<ScopedStatement>> filterNodes(
            Iterable<Node<ScopedStatement>> nodes) {
        ImmutableSet.Builder<Node<ScopedStatement>> builder = ImmutableSet.builder();

        for (Node<ScopedStatement> node : nodes) {
            if (!node.hasValue()) {
                continue;
            }
            if (!node.value().getStatement().hasExpression()) {
                continue;
            }
            builder.add(node);
        }

        return builder.build();
    }

    /**
     * Maps StatementNode<ScopedStatement>s to variables they may change during
     * execution.
     */
    private static Set<ScopedVariable> getPotentiallyChangedVariables(
            Node<ScopedStatement> statementNode) {
        if (!statementNode.hasValue()) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<ScopedVariable> builder = ImmutableSet.builder();
        Set<ScopedVariable> globals = Util.getGlobalVariables(statementNode.value().getScope());
            StaticStatement statement = statementNode.value().getStatement();
            if (statement instanceof Assignment) {
                builder.add(ScopedVariable.getAssigned(
                        (Assignment) statementNode.value().getStatement(), statementNode.value().getScope()));
            }

        if (Util.containsMethodCall(statement.getExpression())) {
            builder.addAll(globals);
        }

        return builder.build();
    }

    @Override
    public boolean gensImmuneToKills() {
        return false;
    }

    /** Available Subexpressions Algorithm propagates forward. */
    public boolean isForward() {
        return true;
    }
}
