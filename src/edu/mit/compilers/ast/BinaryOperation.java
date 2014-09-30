package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class BinaryOperation implements NativeExpression {

    private final BinaryOperator operator;
    private final NativeExpression leftArgument;
    private final NativeExpression rightArgument;

    public BinaryOperation(BinaryOperator operator, NativeExpression leftArgument,
            NativeExpression rightArgument) {
        this.operator = operator;
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(leftArgument, rightArgument);
    }

    @Override
    public String getName() {
        return operator.getSymbol();
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
