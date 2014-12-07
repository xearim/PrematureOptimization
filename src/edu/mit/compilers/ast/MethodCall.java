package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MethodCall extends StaticStatement implements NativeExpression {
	private final String methodName;
    private final NodeSequence<GeneralExpression> parameterValues;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.METHOD_CALL;

    public MethodCall(String methodName, List<GeneralExpression> parameterValues,
            LocationDescriptor locationDescriptor) {
        this.methodName = methodName;
        this.parameterValues = new NodeSequence<GeneralExpression>(parameterValues, "parameters");
        this.locationDescriptor = locationDescriptor;
    }
    
    public MethodCall(String methodName, List<GeneralExpression> parameterValues) {
        this(methodName, parameterValues, LocationDescriptor.machineCode());
    }

    @Override
    protected Optional<NativeExpression> expression() {
        // Method call is both a statement and an expression!
        return Optional.<NativeExpression>of(this);
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

    public String asText() {
    	String out = getMethodName();
    	for(GeneralExpression param: getParameterValues()){
    		out += " " + param.asText();
    	}
    	return out;
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	return this.getType().getPrecedence() - other.getType().getPrecedence();
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

    @Override
    public NativeExpression withReplacements(NativeExpression toReplace,
            NativeExpression replacement) {
        if (this.equals(toReplace)) {
            return replacement;
        }
        List<GeneralExpression> paramsWithReplacements = Lists.newArrayList();
        for (GeneralExpression param : parameterValues) {
            GeneralExpression newParam = (param instanceof NativeExpression)
                    ? ((NativeExpression) param).withReplacements(toReplace, replacement)
                    : param;
            paramsWithReplacements.add(newParam);
        }
        return new MethodCall(methodName, paramsWithReplacements, locationDescriptor);
    }
}
