package edu.mit.compilers.graph;

import com.google.common.base.Optional;

/**
 * A node in a graph.
 *
 * <p>Uniquely identifies a node, even if its value is equal to another nodes' value.
 * So, `Node.of(5).equals(Node.of(5))` is false.
 */
public class Node<T> {
    private final Optional<T> value;

    private Node(Optional<T> value) {
        this.value = value;
    }

    public static <T> Node<T> of(T value) {
        return new Node<T>(Optional.of(value));
    }

    public static <T> Node<T> nop() {
        return new Node<T>(Optional.<T>absent());
    }

    public boolean hasValue() {
        return value.isPresent();
    }

    public T value() {
        return value.get();
    }

    @Override
    public String toString() {
        return hasValue()
                ? "Node [value=" + value() + "]"
                : "Node [NOP]";
    }
}
