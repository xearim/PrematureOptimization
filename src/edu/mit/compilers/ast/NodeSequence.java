package edu.mit.compilers.ast;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A "dummy node" that exists only to group its children.
 *
 * <p>A NodeSequence<T> is basically a List<T> that implements Node.
 */
public class NodeSequence<T extends Node> implements Node, Iterable<T> {

    private ImmutableList<T> sequence;
    private final String name;
    
    public NodeSequence(List<? extends T> sequence, String name) {
        this.sequence = ImmutableList.copyOf(sequence);
        this.name = name;
    }
    
    @Override
    public Iterable<T> getChildren() {
        return sequence;
    }

    @Override
    public String getName() {
        return name;
    }

    public ImmutableList<T> getSequence() {
        return sequence;
    }

    @Override
    public Iterator<T> iterator() {
        return getChildren().iterator();
    }
}
