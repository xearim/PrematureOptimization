package edu.mit.compilers.ast;

import com.google.common.base.Optional;
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

    // Possible return types are limited by the operator type
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		switch(operator){
		case AND:
		case OR:
		case DOUBLE_EQUALS:
		case NOT_EQUALS:
		case GREATER_THAN_OR_EQUAL:
		case GREATER_THAN:
		case LESS_THAN_OR_EQUAL:
		case LESS_THAN:
			if(type.isPresent() && type.get() == BaseType.BOOLEAN)
				return true;
			return false;
		case DIVIDED_BY:
		case MINUS:
		case MODULO:
		case PLUS:
		case TIMES:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		// Should never reach here
		default:
			return false;
		}
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		switch(operator){
		case AND:
		case OR:
		case DOUBLE_EQUALS:
		case NOT_EQUALS:
		case GREATER_THAN_OR_EQUAL:
		case GREATER_THAN:
		case LESS_THAN_OR_EQUAL:
		case LESS_THAN:
			if(type.isPresent() && type.get() == BaseType.BOOLEAN)
				return true;
			return false;
		case DIVIDED_BY:
		case MINUS:
		case MODULO:
		case PLUS:
		case TIMES:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		// Should never reach here
		default:
			return false;
		}
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
