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

    // Return types are special nodes belonging just to a method explaining their return type
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return this.type.equals(type);
	}
	
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return this.type.equals(type);
	}

	// Evaluation of a returnType is meaningless
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
