package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A "dummy node" that exists only to group its children.
 *
 * <p>A NodeSequence<T> is basically a List<T> that implements Node.
 */
public class NodeSequence<T extends Node> implements Node {

    private ImmutableList<? extends T> sequence;
    private final String name;
    
    public NodeSequence(List<? extends T> sequence, String name) {
        this.sequence = ImmutableList.copyOf(sequence);
        this.name = name;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return sequence;
    }

    @Override
    public String getName() {
        return name;
    }

    public ImmutableList<? extends T> getSequence() {
        return sequence;
    }
}
