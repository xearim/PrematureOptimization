package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class Method implements Node {

    private final String name;
    private final ReturnType returnType;
    private final ParameterScope parameters;
    private final Block body;
    private final LocationDescriptor locationDescriptor;

    public Method(String name, ReturnType returnType, ParameterScope parameters,
	    Block body, LocationDescriptor locationDescriptor) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(returnType, body);
    }

    @Override
    public String getName() {
        return name;
    }
    
    public ParameterScope getParameters() {
    	return parameters;
    }
    
    public ReturnType getReturnType(){
    	return returnType;
    }
    
    public ImmutableList<BaseType> getSignature() {
    	return parameters.getSignature();
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }
    
    public Block getBlock() {
    	return body;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
