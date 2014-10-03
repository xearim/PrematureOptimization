package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ArrayLocation implements Location {

    private final String variableName;
    private final NativeExpression index;
    private final LocationDescriptor locationDescriptor;
    
    public ArrayLocation(String variableName, NativeExpression index,
            LocationDescriptor locationDescriptor) {
        this.variableName = variableName;
        this.index = index;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName + "[" + index +"]";
    }
    
    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    public NativeExpression getIndex() {
        return index;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
