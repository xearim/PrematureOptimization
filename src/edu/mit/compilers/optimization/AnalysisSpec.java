package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import edu.mit.compilers.graph.Node;

public interface AnalysisSpec<N, T> {
    public Set<T> getGenSet(Node<N> node);
    public boolean mustKill(Node<N> currentNode, T candidate);
    public Set<T> applyConfluenceOperator(Iterable<Collection<T>> inputs);
    public boolean gensImmuneToKills();
    public Set<Node<N>> filterNodes(Iterable<Node<N>> nodes);
}
