package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class IfStatement implements Statement {

    private final NativeExpression condition;
    private final Block thenBlock;
    private final Optional<Block> elseBlock;
    
    private IfStatement(NativeExpression condition, Block thenBlock, Optional<Block> elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
    
    public static IfStatement ifThen(NativeExpression condition, Block thenBlock) {
        return new IfStatement(condition, thenBlock, Optional.<Block>absent());
    }
    
    public static IfStatement
            ifThenElse(NativeExpression condition, Block thenBlock, Block elseBlock) {
        return new IfStatement(condition, thenBlock, Optional.of(elseBlock));
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        builder.add(condition);
        builder.add(thenBlock);
        if (elseBlock.isPresent()) {
            builder.add(elseBlock.get());
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return "if" + (elseBlock.isPresent() ? "-else" : "");
    }

    // If statements are the superset of their two contained blocks
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(elseBlock.isPresent())
			return elseBlock.get().canReturn(type) || thenBlock.canReturn(type);
		return thenBlock.canReturn(type);
	}

	// If statements must return a value iff
	// A) it has an elseBlock (so it has complete control flow)
	// B) both blocks return the same value
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		if(elseBlock.isPresent())
			return elseBlock.get().mustReturn(type) && thenBlock.mustReturn(type);
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		if(elseBlock.isPresent())
			return ImmutableList.of(thenBlock, elseBlock.get());
		return ImmutableList.of(thenBlock);
	}

	// If statements don't evaluate to anything
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
