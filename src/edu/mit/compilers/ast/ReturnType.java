package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ReturnType implements Node {

    private final Optional<BaseType> type;
    private final LocationDescriptor locationDescriptor;

    private ReturnType(Optional<BaseType> type, LocationDescriptor locationDescriptor) {
        this.type = type;
	this.locationDescriptor = locationDescriptor;
    }
    
    public static ReturnType fromVoid(LocationDescriptor locationDescriptor) {
        return new ReturnType(Optional.of(BaseType.VOID), locationDescriptor);
    }

    public static ReturnType
            fromBaseType(BaseType baseType, LocationDescriptor locationDescriptor) {
        return new ReturnType(Optional.of(baseType), locationDescriptor);
    }
    
    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }
    
    public Optional<BaseType> getReturnType() {
    	return type;
    }

    @Override
    public String getName() {
        return type.isPresent()
                ? type.get().toString()
                : "void";
    }


    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (!(obj instanceof ReturnType)) {
            return false;
        }
        ReturnType other = (ReturnType) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
