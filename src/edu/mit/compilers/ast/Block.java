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
    
    public long getMemorySize() {
    	long size = 0;
    	for(Statement statement: getStatements()){
    		size = size < statement.getMemorySize() ? statement.getMemorySize() : size;
    	}
    	return size + scope.size();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result
                + ((statements == null) ? 0 : statements.hashCode());
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
        if (!(obj instanceof Block)) {
            return false;
        }
        Block other = (Block) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        if (statements == null) {
            if (other.statements != null) {
                return false;
            }
        } else if (!statements.equals(other.statements)) {
            return false;
        }
        return true;
    }
}
