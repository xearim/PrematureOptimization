package edu.mit.compilers.graph;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class MutableGraph<T> implements DiGraph<T> {
    // Every edge (a--b) in the graph has two entries in 'edges':
    // (a->b) and (b->a).  (Except, of course, (a--a) which only has
    // one entry!)
    private final Multimap<Node<T>, Node<T>> edges;

    public MutableGraph() {
        this.edges = HashMultimap.create();
    }

    // TODO(jasonpr): Make an interface containing Graph and MutableGraph.
    public MutableGraph(Graph<T> graph) {
        this();
        for (Node<T> node : graph.getNodes()) {
            for (Node<T> successor : graph.getSuccessors(node)) {
                link(node, successor);
            }
        }
    }

    public MutableGraph<T> link(Node<T> node1, Node<T> node2) {
        edges.put(node1, node2);
        edges.put(node2, node1);
        return this;
    }

    public MutableGraph<T> unlink(Node<T> node1, Node<T> node2) {
        checkState(edges.remove(node1, node2));
        if (!node1.equals(node2)) {
            checkState(edges.remove(node2, node1));
        }
        return this;
    }

    public MutableGraph<T> remove(Node<T> node) {
        // Make a copy to prevent concurrent modification issues.
        ImmutableSet<Node<T>> neighbors = ImmutableSet.copyOf(edges.get(node));
        for (Node<T> neighbor : neighbors) {
            unlink(node, neighbor);
        }
        return this;
    }

    @Override
    public Iterable<Node<T>> getNodes() {
        return edges.keySet();
    }

    @Override
    public Iterable<Node<T>> getPredecessors(Node<T> node) {
        return edges.get(node);
    }

    @Override
    public Iterable<Node<T>> getSuccessors(Node<T> node) {
        return edges.get(node);
    }
}
