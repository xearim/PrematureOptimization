package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class StringLiteral implements GeneralExpression {

    private final String value;
    private final LocationDescriptor locationDescriptor;

    public StringLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "\"" + value + "\"";
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
