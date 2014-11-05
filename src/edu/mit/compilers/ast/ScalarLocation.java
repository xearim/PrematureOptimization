package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.NativeExpression.ExpressionType;
import edu.mit.compilers.common.Variable;

public class ScalarLocation implements Location {

    private final Variable variable;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.SCALAR_LOCATION;

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
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	if(this.getType() != other.getType()){
    		return this.getType().getPrecedence() - other.getType().getPrecedence();
    	} else {
    		ScalarLocation otherScalar = (ScalarLocation) other;
    		return this.variable.compareTo(otherScalar.getVariable());
    	}
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

    @Override
    public String toString() {
        return variable.generateName();
    }
}
