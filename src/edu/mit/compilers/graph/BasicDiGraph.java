package edu.mit.compilers.graph;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class BasicDiGraph<T> implements DiGraph<T> {

    private final Multimap<Node<T>, Node<T>> forwardEdges;
    private final Multimap<Node<T>, Node<T>> backwardEdges;

    private BasicDiGraph(ImmutableMultimap<Node<T>, Node<T>> edges) {
        this.forwardEdges = edges;
        this.backwardEdges = edges.inverse();
    }

    @Override
    public Set<Node<T>> getNodes() {
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

        public BasicDiGraph<T> build() {
            return new BasicDiGraph<T>(ImmutableMultimap.copyOf(forwardEdges));
        }
    }
}
