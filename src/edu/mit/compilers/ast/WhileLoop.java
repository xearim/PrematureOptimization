package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class WhileLoop implements Statement {

    private final NativeExpression condition;
    private final Optional<IntLiteral> maxRepetitions;
    private final Block body;
    private final LocationDescriptor locationDescriptor;

    private WhileLoop(NativeExpression condition, Optional<IntLiteral> maxRepetitions,
            Block body, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.maxRepetitions = maxRepetitions;
        this.body = body;
	this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        builder.add(condition);
        if (maxRepetitions.isPresent()) {
            builder.add(maxRepetitions.get());
        }
        builder.add(body);
        return builder.build();
    }

    @Override
    public String getName() {
        return "while";
    }

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of(body);
	}

	@Override
	public boolean canReturn() {
		for(Statement subStatement: body.getStatements()){
			if(subStatement.canReturn())
				return true;
		}
		return false;
	}
	
	@Override
	public long getMemorySize() {
		return body.getMemorySize();
	}

    public static WhileLoop
	simple(NativeExpression condition, Block body, LocationDescriptor locationDescriptor) {
        return new WhileLoop(condition, Optional.<IntLiteral> absent(), body, locationDescriptor);
    }
    
    public static WhileLoop limited(NativeExpression condition, IntLiteral maxRepetitions,
            Block body, LocationDescriptor locationDescriptor) {
        return new WhileLoop(condition, Optional.of(maxRepetitions), body, locationDescriptor);
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getCondition() {
        return condition;
    }

    public Optional<IntLiteral> getMaxRepetitions() {
        return maxRepetitions;
    }
    
    public Block getBody() {
    	return body;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
