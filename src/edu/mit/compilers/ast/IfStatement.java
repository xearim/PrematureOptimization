package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class IfStatement implements Statement {

    private final NativeExpression condition;
    private final Block thenBlock;
    private final Optional<Block> elseBlock;
    private final LocationDescriptor locationDescriptor;

    private IfStatement(NativeExpression condition, Block thenBlock,
	    Optional<Block> elseBlock, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
	this.locationDescriptor = locationDescriptor;
    }
    
    public static IfStatement ifThen(NativeExpression condition, Block thenBlock,
            LocationDescriptor locationDescriptor) {
        return new IfStatement(
                condition, thenBlock, Optional.<Block> absent(), locationDescriptor);
    }
    
    public static IfStatement ifThenElse(NativeExpression condition, Block thenBlock,
            Block elseBlock, LocationDescriptor locationDescriptor) {
        return new IfStatement(condition, thenBlock, Optional.of(elseBlock), locationDescriptor);
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

	@Override
	public Iterable<Block> getBlocks() {
		if(elseBlock.isPresent())
			return ImmutableList.of(thenBlock, elseBlock.get());
		return ImmutableList.of(thenBlock);
	}

	@Override
	public boolean canReturn() {
		for(Statement subStatement: thenBlock.getStatements()){
			if(subStatement.canReturn())
				return true;
		}
		if(elseBlock.isPresent())
			for(Statement subStatement: elseBlock.get().getStatements()){
				if(subStatement.canReturn())
					return true;
			}
		return false;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getCondition() {
        return condition;
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
