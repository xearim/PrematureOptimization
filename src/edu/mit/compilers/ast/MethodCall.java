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
    public Iterable<? extends GeneralExpression> getChildren() {
        return parameterValues.getChildren();
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
	
	@Override
	public long getMemorySize() {
		return 0;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result
                + ((parameterValues == null) ? 0 : parameterValues.hashCode());
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
        if (!(obj instanceof MethodCall)) {
            return false;
        }
        MethodCall other = (MethodCall) obj;
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        if (parameterValues == null) {
            if (other.parameterValues != null) {
                return false;
            }
        } else if (!parameterValues.equals(other.parameterValues)) {
            return false;
        }
        return true;
    }
}
