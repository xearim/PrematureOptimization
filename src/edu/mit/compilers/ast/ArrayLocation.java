package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.common.Variable;

public class ArrayLocation implements Location {

    private final Variable variable;
    private final NativeExpression index;
    private final LocationDescriptor locationDescriptor;
    
    public ArrayLocation(Variable variable, NativeExpression index,
            LocationDescriptor locationDescriptor) {
        this.variable = variable;
        this.index = index;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of(index);
    }

    @Override
    public String getName() {
        return variable + "[" + index +"]";
    }
    
    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    public NativeExpression getIndex() {
        return index;
    }
    
    public String asText() {
    	return variable.asText() + "[" + index.asText() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
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
        if (variable == null) {
            if (other.variable != null) {
                return false;
            }
        } else if (!variable.equals(other.variable)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return variable.asText() + "[" + index + "]";
    }

    @Override
    public NativeExpression withReplacements(NativeExpression toReplace,
            NativeExpression replacement) {
        if (this.equals(toReplace)) {
            return replacement;
        }
        return new ArrayLocation(variable,
                index.withReplacements(toReplace, replacement),
                locationDescriptor);
    }
}
