package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.intersection;
import static edu.mit.compilers.optimization.Util.containsMethodCall;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;
import static edu.mit.compilers.optimization.Util.getRedefinedVariables;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.NativeExpression;
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
        Set<ScopedVariable> victimVariables = getRedefinedVariables(curNode);
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
        return filterNodesWithoutExpressions(nodes);
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
