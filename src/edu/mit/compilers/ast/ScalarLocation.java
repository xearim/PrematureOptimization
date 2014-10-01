package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ScalarLocation implements Location {

    private final String variableName;
    
    public ScalarLocation(String variableName) {
        this.variableName = variableName;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
