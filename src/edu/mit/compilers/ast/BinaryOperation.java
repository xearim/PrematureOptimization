package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class BinaryOperation implements NativeExpression {

    private final BinaryOperator operator;
    private final NativeExpression leftArgument;
    private final NativeExpression rightArgument;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.BINARY_OPERATION;

    public BinaryOperation(BinaryOperator operator, NativeExpression leftArgument,
            NativeExpression rightArgument, LocationDescriptor locationDescriptor) {
        this.operator = operator;
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
        this.locationDescriptor = locationDescriptor;
    }
    
    public BinaryOperation(BinaryOperator operator, NativeExpression leftArgument,
            NativeExpression rightArgument){
    	this(operator, leftArgument, rightArgument, LocationDescriptor.machineCode());
    }
    
    @Override
    public Iterable<NativeExpression> getChildren() {
        return ImmutableList.of(leftArgument, rightArgument);
    }

    @Override
    public String getName() {
        return operator.getSymbol();
    }
    
    public BinaryOperator getOperator() {
    	return operator;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getLeftArgument() {
        return leftArgument;
    }

    public NativeExpression getRightArgument() {
        return rightArgument;
    }
    
    public String asText() {
    	return leftArgument.asText() + " " + operator.getSymbol() + " " + rightArgument.asText();
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	return this.getType().getPrecedence() - other.getType().getPrecedence();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((leftArgument == null) ? 0 : leftArgument.hashCode());
        result = prime * result
                + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result
                + ((rightArgument == null) ? 0 : rightArgument.hashCode());
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
        if (!(obj instanceof BinaryOperation)) {
            return false;
        }
        BinaryOperation other = (BinaryOperation) obj;
        if (leftArgument == null) {
            if (other.leftArgument != null) {
                return false;
            }
        } else if (!leftArgument.equals(other.leftArgument)) {
            return false;
        }
        if (operator != other.operator) {
            return false;
        }
        if (rightArgument == null) {
            if (other.rightArgument != null) {
                return false;
            }
        } else if (!rightArgument.equals(other.rightArgument)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + leftArgument + " " + operator + " " + rightArgument + "]";
    }
}
