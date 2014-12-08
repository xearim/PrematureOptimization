package edu.mit.compilers.graph;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * An undirected graph.  That is, a digraph with the constraint that for every
 * edge (a->b), there is an edge (b->a), too.
 */
public class Graph<T> implements DiGraph<T> {

    private final BasicDiGraph<T> delegate;
    private final Set<Node<T>> nodes;

    private Graph(BasicDiGraph<T> delegate, Set<Node<T>> nodes) {
        this.delegate = delegate;
        this.nodes = nodes;
    }

    @Override
    public Set<Node<T>> getNodes() {
        return ImmutableSet.copyOf(nodes);
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
        private final ImmutableSet.Builder<Node<T>> nodes;
        private Builder() {
            this.delegate = BasicDiGraph.builder();
            this.nodes = ImmutableSet.builder();
        }

        public Builder<T> link(Node<T> end1, Node<T> end2) {
            nodes.add(end1);
            nodes.add(end2);
            delegate.link(end1, end2);
            delegate.link(end2, end1);
            return this;
        }

        public Builder<T> addNode(Node<T> node) {
            nodes.add(node);
            return this;
        }

        public Graph<T> build() {
            return new Graph<T>(delegate.build(), nodes.build());
        }
    }
}
