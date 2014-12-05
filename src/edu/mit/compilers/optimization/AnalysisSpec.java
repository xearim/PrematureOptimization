package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public interface AnalysisSpec<T> {
    public boolean isForward();
    public Set<T> getGenSet(Node<ScopedStatement> node);
    public boolean mustKill(Node<ScopedStatement> currentNode, T candidate);
    public Set<T> applyConfluenceOperator(Iterable<Collection<T>> inputs);
    public boolean gensImmuneToKills();
    public Set<Node<ScopedStatement>> filterNodes(Iterable<Node<ScopedStatement>> nodes);
}
