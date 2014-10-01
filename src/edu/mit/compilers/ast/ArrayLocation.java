package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ArrayLocation implements Location {

    private final String variableName;
    private final int index;
    
    public ArrayLocation(String variableName, int index) {
        this.variableName = variableName;
        this.index = index;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName + "[" + index +"]";
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
