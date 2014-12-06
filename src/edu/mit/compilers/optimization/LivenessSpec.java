package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;
import static edu.mit.compilers.optimization.Util.getRedefinedVariables;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class LivenessSpec implements AnalysisSpec<ScopedStatement, ScopedVariable> {

    /** Liveness algorithm propagates backwards */
    public boolean isForward() {
        return false;
    }

    /** A Variable is generated if it is used by this statement */
    @Override
    public Set<ScopedVariable> getGenSet(Node<ScopedStatement> node) {
        if (!node.hasValue()) {
            return ImmutableSet.<ScopedVariable>of();
        }
        return ScopedVariable.getVariablesOf(node.value());
    }

    /** Kills a ScopedVariable if it was defined. */
    @Override
    public boolean mustKill(Node<ScopedStatement> currentNode,
            ScopedVariable candidate) {
        return getRedefinedVariables(currentNode).contains(candidate);
    }

    @Override
    public Set<ScopedVariable> applyConfluenceOperator(
            Iterable<Collection<ScopedVariable>> inputs) {
        return union(inputs);
    }

    @Override
    public boolean gensImmuneToKills() {
        return true;
    }

    @Override
    public Set<Node<ScopedStatement>> filterNodes(
            Iterable<Node<ScopedStatement>> nodes) {
        return filterNodesWithoutExpressions(nodes);
    }

}
