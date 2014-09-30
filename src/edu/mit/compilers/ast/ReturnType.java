package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ReturnType implements Node {

    private final Optional<BaseType> type;
    
    private ReturnType(Optional<BaseType> type) {
        this.type = type;
    }
    
    public static ReturnType fromVoid() {
        return new ReturnType(Optional.<BaseType>absent());
    }

    public static ReturnType fromBaseType(BaseType baseType) {
        return new ReturnType(Optional.of(baseType));
    }
    
    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return type.isPresent()
                ? type.get().toString()
                : "void";
    }

    // Return types return just one type, equal to their own private type
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(!type.isPresent() && !this.type.isPresent())
			return true;
		return type.get() == this.type.get();
	}
	
    // Return types return just one type, equal to their own private type
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		if(!type.isPresent() && !this.type.isPresent())
			return true;
		return type.get() == this.type.get();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
