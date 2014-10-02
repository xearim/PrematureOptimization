package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class UnaryOperation implements NativeExpression {

    private final UnaryOperator operator;
    private final NativeExpression argument;

    public UnaryOperation(UnaryOperator operator, NativeExpression argument) {
        this.operator = operator;
        this.argument = argument;
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

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
