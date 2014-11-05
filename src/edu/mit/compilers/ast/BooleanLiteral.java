package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.NativeExpression.ExpressionType;

public class BooleanLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final long longValue;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.BOOLEAN_LITERAL;

    public BooleanLiteral(String value, LocationDescriptor locationDescriptor) {
        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        checkArgument(isTrue || isFalse);

        this.value = value;
        this.longValue = (isTrue) ? 1 : 0;
        this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public long get64BitValue() {
        return longValue;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public boolean equals(BooleanLiteral bl) {
        return this.getName().equals(bl.getName())
                && this.get64BitValue() == bl.get64BitValue()
                && this.getLocationDescriptor().equals(bl.getLocationDescriptor());
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	if(this.getType() != other.getType()){
    		return this.getType().getPrecedence() - other.getType().getPrecedence();
    	} else {
    		BooleanLiteral otherBoolean = (BooleanLiteral) other;
    		return (int) (this.get64BitValue() - otherBoolean.get64BitValue());
    	}
    }

    public int hashCode() {
        // TODO(Manny): figure out how to not have to cast a long to an int
        return (int) this.longValue;
    }

    public String toString() {
        return value;
    }
    
    public String asText() {
    	return toString();
    }
}
