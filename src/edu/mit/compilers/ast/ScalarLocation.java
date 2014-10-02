package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ScalarLocation implements Location {

    private final String variableName;
    private final LocationDescriptor locationDescriptor;

    public ScalarLocation(String variableName, LocationDescriptor locationDescriptor) {
        this.variableName = variableName;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
