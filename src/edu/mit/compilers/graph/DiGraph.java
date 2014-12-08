package edu.mit.compilers.graph;

import java.util.Set;

public interface DiGraph<T> {
    public Set<Node<T>> getNodes();
    public Iterable<Node<T>> getPredecessors(Node<T> node);
    public Iterable<Node<T>> getSuccessors(Node<T> node);
}
