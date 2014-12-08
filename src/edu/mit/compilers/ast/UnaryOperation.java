package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class UnaryOperation implements NativeExpression {

    private final UnaryOperator operator;
    private final NativeExpression argument;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.UNARY_OPERATION;

    public UnaryOperation(UnaryOperator operator, NativeExpression argument,
            LocationDescriptor locationDescriptor) {
        this.operator = operator;
        this.argument = argument;
        this.locationDescriptor = locationDescriptor;
    }
    
    public UnaryOperation(UnaryOperator operator, NativeExpression argument) {
        this(operator, argument, LocationDescriptor.machineCode());
    }
    
    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of(argument);
    }

    @Override
    public String getName() {
        return operator.getSymbol();
    }
    
    public UnaryOperator getOperator() {
    	return operator;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getArgument() {
        return argument;
    }
    
    public String asText() {
    	return operator.getSymbol() + " " + argument.asText();
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	if(this.getType() != other.getType()){
    		return this.getType().getPrecedence() - other.getType().getPrecedence();
    	} else {
    		return this.argument.compareTo(((UnaryOperation) other).getArgument());
    	}
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argument == null) ? 0 : argument.hashCode());
        result = prime * result
                + ((operator == null) ? 0 : operator.hashCode());
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
        if (!(obj instanceof UnaryOperation)) {
            return false;
        }
        UnaryOperation other = (UnaryOperation) obj;
        if (argument == null) {
            if (other.argument != null) {
                return false;
            }
        } else if (!argument.equals(other.argument)) {
            return false;
        }
        if (operator != other.operator) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "[" + asText() + "]";
    }

    @Override
    public NativeExpression withReplacements(NativeExpression toReplace,
            NativeExpression replacement) {
        if (this.equals(toReplace)) {
            return replacement;
        }
        return new UnaryOperation(operator,
                argument.withReplacements(toReplace, replacement),
                locationDescriptor);
    }
}
