package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class IntLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final long longValue;
    private final LocationDescriptor locationDescriptor;

    public IntLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
        this.longValue = Long.parseLong(value);
        this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public long get64BitValue() {
        return longValue;
    }

    public boolean equals(IntLiteral il) {
        return this.value.equals(il.getName())
                && this.get64BitValue() == il.get64BitValue()
                && this.getLocationDescriptor().equals(il.getLocationDescriptor());
    }

    // TODO(jasonpr): Implement hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
