package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import edu.mit.compilers.graph.Node;

public interface AnalysisSpec<N, T> {
    public boolean isForward();
    public Set<T> getGenSet(Node<N> node, Collection<T> inputs);
    public boolean mustKill(Node<N> currentNode, T candidate);
    public Set<T> applyConfluenceOperator(Iterable<Collection<T>> inputs);
    public boolean gensImmuneToKills();
}
