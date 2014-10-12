package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;

public class BooleanLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final LocationDescriptor locationDescriptor;

    public BooleanLiteral(String value, LocationDescriptor locationDescriptor) {
        checkArgument(value.equals("true") || value.equals("false"));
        this.value = value;
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
        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
