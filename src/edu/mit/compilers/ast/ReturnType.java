package edu.mit.compilers.ast;

import sun.reflect.generics.tree.BaseType;

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

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
