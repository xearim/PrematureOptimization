package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Assignment implements Statement {

    private final Location location;
    private final AssignmentOperation operation;
    private final NativeExpression expression;
    
    public Assignment(
            Location location, AssignmentOperation operation, NativeExpression expression) {
        this.location = location;
        this.operation = operation;
        this.expression = expression;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(location, expression);
    }

    @Override
    public String getName() {
        return operation.getSymbol();
    }

    // Assignments do not produce return values
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	// Assignments do not produce return values
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	// Assignments do not evaluate to any value
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
