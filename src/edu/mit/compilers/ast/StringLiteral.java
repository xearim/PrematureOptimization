package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class StringLiteral implements GeneralExpression {

    private final String value;
    
    public StringLiteral(String value) {
        this.value = value;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "\"" + value + "\"";
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
