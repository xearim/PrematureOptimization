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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result
                + ((variableName == null) ? 0 : variableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ArrayLocation)) {
            return false;
        }
        ArrayLocation other = (ArrayLocation) obj;
        if (index == null) {
            if (other.index != null) {
                return false;
            }
        } else if (!index.equals(other.index)) {
            return false;
        }
        if (variableName == null) {
            if (other.variableName != null) {
                return false;
            }
        } else if (!variableName.equals(other.variableName)) {
            return false;
        }
        return true;
    }
}
