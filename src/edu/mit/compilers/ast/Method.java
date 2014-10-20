package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.Architecture;

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
    
    public boolean isMain() {
    	return getName().equals(Architecture.MAIN_METHOD_NAME);
    }
    
    public boolean isVoid() {
    	if(returnType.getReturnType().isPresent()){
    		return returnType.getReturnType().get().isA(BaseType.VOID);
    	}
    	throw new AssertionError("Somehow we have a method without a return type what?");
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
