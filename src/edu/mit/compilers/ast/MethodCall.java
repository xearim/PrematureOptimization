package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MethodCall implements Statement, NativeExpression {

    private final String methodName;
    private final NodeSequence<GeneralExpression> parameterValues;
    private final LocationDescriptor locationDescriptor;

    public MethodCall(String methodName, List<GeneralExpression> parameterValues,
            LocationDescriptor locationDescriptor) {
        this.methodName = methodName;
        this.parameterValues = new NodeSequence<GeneralExpression>(parameterValues, "parameters");
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(parameterValues);
    }

    @Override
    public String getName() {
        return "call " + methodName;
    }

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}
	
	@Override
	public boolean canReturn() {
		return false;
	}
	
	public String getMethodName(){
		return methodName;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NodeSequence<GeneralExpression> getParameterValues() {
        return parameterValues;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
