package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Block implements Node {
    
    private final String name;
    private final NodeSequence<FieldDeclaration> locals;
    private final NodeSequence<Statement> statements;
    
    public Block(String name, List<FieldDeclaration> locals, List<Statement> statements) {
        this.name = name;
        this.locals = new NodeSequence<FieldDeclaration>(locals, "locals");
        this.statements = new NodeSequence<Statement>(statements, "statements");
    }
    
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(locals, statements);
    }

    @Override
    public String getName() {
        return name;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
