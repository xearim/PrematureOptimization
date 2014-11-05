package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.common.Variable;

public class ScalarLocation implements Location {

    private final Variable variable;
    private final LocationDescriptor locationDescriptor;

    public ScalarLocation(Variable variable, LocationDescriptor locationDescriptor) {
        this.variable = variable;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    @Override
    public String getName() {
        return variable.generateName();
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }
    
    public String asText() {
    	return variable.asText();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((variable == null) ? 0 : variable.hashCode());
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
        if (variable == null) {
            if (other.variable != null) {
                return false;
            }
        } else if (!variable.equals(other.variable)) {
            return false;
        }
        return true;
    }
}
