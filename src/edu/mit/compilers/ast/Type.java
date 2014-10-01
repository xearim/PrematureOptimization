package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Type implements Node {

    private final BaseType baseType;
    
    // The Optional is present when this is an array type, and absent when it's a scalar.
    private final Optional<Integer> length;
    
    private Type(BaseType baseType, Optional<Integer> length) {
        this.baseType = baseType;
        this.length = length;
    }
    
    public static Type scalar(BaseType baseType) {
        return new Type(baseType, Optional.<Integer>absent());
    }
    
    public static Type array(BaseType baseType, int length) {
        return new Type(baseType, Optional.of(length));
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        String base = baseType.toString();
        String reach = length.isPresent()
                ? "[" + length.get() + "]"
                : "";
        return base + reach;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
