package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class Callout implements Node {

    private final String name;
    
    public Callout(String name) {
        this.name = name;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return name;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
