package edu.mit.compilers.graph;

import java.util.Set;

/**
 * An undirected graph.  That is, a digraph with the constraint that for every
 * edge (a->b), there is an edge (b->a), too.
 */
public class Graph<T> implements DiGraph<T> {

    private final BasicDiGraph<T> delegate;

    private Graph(BasicDiGraph<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Set<Node<T>> getNodes() {
        return delegate.getNodes();
    }

    @Override
    public Iterable<Node<T>> getPredecessors(Node<T> node) {
        return delegate.getPredecessors(node);
    }

    @Override
    public Iterable<Node<T>> getSuccessors(Node<T> node) {
        return delegate.getSuccessors(node);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> {
        private final BasicDiGraph.Builder<T> delegate;
        private Builder() {
            this.delegate = BasicDiGraph.builder();
        }

        public Builder<T> link(Node<T> end1, Node<T> end2) {
            delegate.link(end1, end2);
            delegate.link(end2, end1);
            return this;
        }

        public Graph<T> build() {
            return new Graph<T>(delegate.build());
        }
    }
}
