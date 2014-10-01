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
		return false;
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Optional<BaseType> evalType() {
		switch(operator){
		case ARRAY_LENGTH:
			return Optional.of(BaseType.INTEGER);
		case NEGATIVE:
			return Optional.of(BaseType.INTEGER);
		case NOT:
			return Optional.of(BaseType.BOOLEAN);
		default:
			return Optional.absent();
		}
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
