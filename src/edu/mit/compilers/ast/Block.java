package edu.mit.compilers.ast;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;
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

    // Blocks can only return the same type that they must return
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return mustReturn(type);
	}

	// A block must return a value of a specifc type iff:
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
		if(!type.isPresent())
			return true;
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
