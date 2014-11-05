package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.NativeExpression.ExpressionType;
import edu.mit.compilers.common.Variable;

public class ArrayLocation implements Location {

    private final Variable variable;
    private final NativeExpression index;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.ARRAY_LOCATION;
    
    public ArrayLocation(Variable variable, NativeExpression index,
            LocationDescriptor locationDescriptor) {
        this.variable = variable;
        this.index = index;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of();
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
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	if(this.getType() != other.getType()){
    		return this.getType().getPrecedence() - other.getType().getPrecedence();
    	} else {
    		ArrayLocation otherArray = (ArrayLocation) other;
    		if(this.variable.compareTo(otherArray.getVariable()) == 0){
    			return this.index.compareTo(otherArray.getIndex());
    		} else {
    			return this.variable.compareTo(otherArray.getVariable());
    		}
    	}
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
}
