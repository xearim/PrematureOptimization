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
    public String getVariableName() {
        return variableName;
    }

    @Override
    public String getName() {
        return variableName;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (!(obj instanceof ScalarLocation)) {
            return false;
        }
        ScalarLocation other = (ScalarLocation) obj;
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
