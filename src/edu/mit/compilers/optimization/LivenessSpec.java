package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;

import java.util.Collection;
import java.util.Set;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class LivenessSpec implements AnalysisSpec<ScopedStatement, ScopedVariable> {

    /** Liveness algorithm propagates backwards */
    public boolean isForward() {
        return false;
    }

    @Override
    public Set<ScopedVariable> getGenSet(Node<ScopedStatement> node) {
        throw new UnsupportedOperationException("unimplemented");
    }

    /** Kills a ScopedVariable if it was used in that node's expression. */
    @Override
    public boolean mustKill(Node<ScopedStatement> currentNode,
            ScopedVariable candidate) {
        throw new UnsupportedOperationException("unimplemented");
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
