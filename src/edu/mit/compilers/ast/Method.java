package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Method implements Node {

    private final String name;
    private final ReturnType returnType;
    private final ParameterScope parameters;
    private final Block body;

    public Method(String name, ReturnType returnType, List<FieldDescriptor> parameters, Block body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ParameterScope(parameters);
        this.body = body;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(returnType, body);
    }

    @Override
    public String getName() {
        return name;
    }
    
    public Scope getParameters() {
    	return parameters;
    }
    
    public ImmutableList<BaseType> getSignature() {
    	return parameters.getSignature();
    }

    // A method's return is determined entirely by its return type
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return returnType.canReturn(type);
	}

	// A method's return is determined entirely by its return type
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return returnType.mustReturn(type);
	}

	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
