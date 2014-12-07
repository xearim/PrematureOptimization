package edu.mit.compilers.graph;

public interface DiGraph<T> {
    public Iterable<Node<T>> getNodes();
    public Iterable<Node<T>> getPredecessors(Node<T> node);
    public Iterable<Node<T>> getSuccessors(Node<T> node);
}
