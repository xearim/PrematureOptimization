package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class MethodCall implements Statement {

    private final String methodName;
    private final NodeSequence<GeneralExpression> parameterValues;
    
    public MethodCall(String methodName, List<GeneralExpression> parameterValues) {
        this.methodName = methodName;
        this.parameterValues = new NodeSequence<GeneralExpression>(parameterValues, "parameters");
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(parameterValues);
    }

    @Override
    public String getName() {
        return "call " + methodName;
    }

    // a method call evaluates to something {int, bool, void} but for
    // all intents and purposes is a variable, thus it doesn't have a return value
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	// A method must be looked up to know its evaluation type
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
