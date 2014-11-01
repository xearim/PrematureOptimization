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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result
                + ((returnType == null) ? 0 : returnType.hashCode());
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
        if (!(obj instanceof Method)) {
            return false;
        }
        Method other = (Method) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }
}
