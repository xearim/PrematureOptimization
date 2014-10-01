package edu.mit.compilers.ast;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Block implements Node {
    
    private final String name;
    private final Scope scope;
    private final NodeSequence<Statement> statements;
    
    public Block(String name, List<FieldDescriptor> scope, List<Statement> statements) {
        this.name = name;
        this.scope = new Scope(scope);
        this.statements = new NodeSequence<Statement>(statements, "statements");
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
    
    public Iterable<Statement> getStatements(){
    	ImmutableList.Builder<Statement> builder = ImmutableList.builder();
    	for(Node n : statements.getChildren())
    		// The only nodes inside of a block should be statements, so this cast should be safe
    		builder.add((Statement) n);
    	return builder.build();
    }

    // Blocks can only return the same type that they must return
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return mustReturn(type);
	}

	// A block must return a value of a specific type iff:
	// a) said block has a return statement for that type
	// b) said block has no return statement for any other type encountered before the target type
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		Iterator<? extends Node> blockContents = statements.getChildren().iterator();
		for(Node block = blockContents.next(); blockContents.hasNext();){
			if((block.mustReturn(Optional.of(BaseType.BOOLEAN)) || 
			    block.mustReturn(Optional.of(BaseType.INTEGER)) ||
			    block.mustReturn(Optional.<BaseType>absent()) ) && !block.mustReturn(type))
				return false;
			else if(block.mustReturn(type))
				return true;
		}
		return false;
	}


	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
