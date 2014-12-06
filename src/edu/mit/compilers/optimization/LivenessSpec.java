package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;

import java.util.Collection;
import java.util.Set;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.Node;

public class LivenessSpec implements AnalysisSpec<ScopedStatement, Variable> {

    /** Liveness algorithm propagates backwards */
    public boolean isForward() {
        return false;
    }

    @Override
    public Set<Variable> getGenSet(Node<ScopedStatement> node) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean mustKill(Node<ScopedStatement> currentNode,
            Variable candidate) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public Set<Variable> applyConfluenceOperator(
            Iterable<Collection<Variable>> inputs) {
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
