package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class FieldDeclaration implements Node {
    
    private final String name;
    private final Type type;

    public FieldDeclaration(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(type);
    }

    @Override
    public String getName() {
        return name;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.    
}
