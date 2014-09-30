package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Method implements Node {

    private final String name;
    private final ReturnType returnType;
    private final NodeSequence<FieldDeclaration> parameters;
    private final Block body;

    public Method(String name, ReturnType returnType,
            List<FieldDeclaration> parameters, Block body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new NodeSequence<FieldDeclaration>(parameters, "parameters");
        this.body = body;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(returnType, parameters, body);
    }

    @Override
    public String getName() {
        return name;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
