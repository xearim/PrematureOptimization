package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class BinaryOperation implements NativeExpression {

    private final BinaryOperator operator;
    private final NativeExpression leftArgument;
    private final NativeExpression rightArgument;
    private final LocationDescriptor locationDescriptor;

    public BinaryOperation(BinaryOperator operator, NativeExpression leftArgument,
            NativeExpression rightArgument, LocationDescriptor locationDescriptor) {
        this.operator = operator;
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
        this.locationDescriptor = locationDescriptor;
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
}
