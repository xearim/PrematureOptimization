package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Block implements Node {
    
    private final String name;
    private final Scope scope;
    private final NodeSequence<Statement> statements;
    private final LocationDescriptor locationDescriptor;

    public Block(String name, Scope scope, List<Statement> statements, LocationDescriptor locationDescriptor) {
        this.name = name;
        this.scope = scope;
        this.statements = new NodeSequence<Statement>(statements, "statements");
	this.locationDescriptor = locationDescriptor;
    }
    
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(statements);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public Scope getScope() {
    	return scope;
    }
    
    // TODO: (jasonpr) update this when getChildren of nodeSequence has a more specific return type
    public Iterable<Statement> getStatements(){
    	ImmutableList.Builder<Statement> builder = ImmutableList.builder();
    	for(Node n : statements.getChildren())
    		// The only nodes inside of a block should be statements, so this cast should be safe
    		builder.add((Statement) n);
    	return builder.build();
    }

    public LocationDescriptor getLocationDescriptor() {
	return locationDescriptor;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
