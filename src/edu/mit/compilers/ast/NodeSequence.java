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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((sequence == null) ? 0 : sequence.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NodeSequence)) {
            return false;
        }
        NodeSequence other = (NodeSequence) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sequence == null) {
            if (other.sequence != null) {
                return false;
            }
        } else if (!sequence.equals(other.sequence)) {
            return false;
        }
        return true;
    }
}
