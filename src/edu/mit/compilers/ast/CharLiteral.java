package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class CharLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final LocationDescriptor locationDescriptor;

    public CharLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
	this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "'" + value + "'";
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
