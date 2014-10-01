package edu.mit.compilers.ast;

import com.google.common.base.Optional;
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

	@Override
	public boolean canReturn(Optional<BaseType> type) {
		switch(operator){
		case ARRAY_LENGTH:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		case NEGATIVE:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		case NOT:
			if(type.isPresent() && type.get() == BaseType.BOOLEAN)
				return true;
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		switch(operator){
		case ARRAY_LENGTH:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		case NEGATIVE:
			if(type.isPresent() && type.get() == BaseType.INTEGER)
				return true;
			return false;
		case NOT:
			if(type.isPresent() && type.get() == BaseType.BOOLEAN)
				return true;
			return false;
		default:
			return false;
		}
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
