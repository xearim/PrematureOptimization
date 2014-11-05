package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.NativeExpression.ExpressionType;
import edu.mit.compilers.semantics.Utils;

public class IntLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final long longValue;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.INT_LITERAL;

    public IntLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
        // We are going to give the int literal the value 0 if its not valid
        // The semantic check should catch it
        this.longValue = Utils.isWithinBounds(value) ? Utils.parseLong(value) : 0L;
        this.locationDescriptor = locationDescriptor;
    }
    
    // Create a machine literal, pretty useful IMHO
    public IntLiteral(Long value) {
    	this.value = Long.toString(value);
    	this.longValue = value;
    	this.locationDescriptor = LocationDescriptor.machineCode();
    }

    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public long get64BitValue() {
        return longValue;
    }
    
    public String asText() {
    	return value;
    }

    public boolean equals(IntLiteral il) {
        return this.value.equals(il.getName())
                && this.get64BitValue() == il.get64BitValue()
                && this.getLocationDescriptor().equals(il.getLocationDescriptor());
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	if(this.getType() != other.getType()){
    		return this.getType().getPrecedence() - other.getType().getPrecedence();
    	} else {
    		IntLiteral otherInt = (IntLiteral) other;
    		if(this.get64BitValue() < otherInt.get64BitValue()){
    			return -1;
    		} else if(this.get64BitValue() > otherInt.get64BitValue()){
    			return 1;
    		} else {
    			return 0;
    		}
    	}
    }

    // For equals and hashCode, we use the longValue, not the value.
    // Thus, "0x1" and "1" will be considered equal, as they should be.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
        if (!(obj instanceof IntLiteral)) {
            return false;
        }
        IntLiteral other = (IntLiteral) obj;
        if (longValue != other.longValue) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + value + ")";
    }
}
