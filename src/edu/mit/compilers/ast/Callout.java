package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class Callout implements Node {

    private final String name;
    private final LocationDescriptor locationDescriptor;

    public Callout(String name, LocationDescriptor locationDescriptor) {
        this.name = name;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return name;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
