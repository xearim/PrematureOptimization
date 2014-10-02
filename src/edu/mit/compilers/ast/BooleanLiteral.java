package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;

public class BooleanLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    
    public BooleanLiteral(String value) {
        checkArgument(value.equals("true") || value.equals("false"));
        this.value = value;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
