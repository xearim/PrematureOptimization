package edu.mit.compilers.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class BasicGraph<T> implements Graph<T> {

    private final Multimap<Node<T>, Node<T>> forwardEdges;
    private final Multimap<Node<T>, Node<T>> backwardEdges;

    private BasicGraph(ImmutableMultimap<Node<T>, Node<T>> edges) {
        this.forwardEdges = edges;
        this.backwardEdges = edges.inverse();
    }

    @Override
    public Iterable<Node<T>> getNodes() {
        return Sets.union(
                forwardEdges.keySet(),
                backwardEdges.keySet());
    }

    @Override
    public Iterable<Node<T>> getPredecessors(Node<T> node) {
        return backwardEdges.get(node);
    }

    @Override
    public Iterable<Node<T>> getSuccessors(Node<T> node) {
        return forwardEdges.get(node);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> {
        private final Multimap<Node<T>, Node<T>> forwardEdges;

        private Builder() {
            this.forwardEdges = HashMultimap.create();
        }

        public Builder<T> link(Node<T> source, Node<T> sink) {
            forwardEdges.put(source, sink);
            return this;
        }

        public BasicGraph<T> build() {
            return new BasicGraph<T>(ImmutableMultimap.copyOf(forwardEdges));
        }
    }
}
