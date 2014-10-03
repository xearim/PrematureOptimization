package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class UnaryOperation implements NativeExpression {

    private final UnaryOperator operator;
    private final NativeExpression argument;
    private final LocationDescriptor locationDescriptor;

    public UnaryOperation(UnaryOperator operator, NativeExpression argument,
            LocationDescriptor locationDescriptor) {
        this.operator = operator;
        this.argument = argument;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
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

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
