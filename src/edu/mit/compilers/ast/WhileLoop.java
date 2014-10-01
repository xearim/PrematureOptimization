package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class WhileLoop implements Statement {

    private final NativeExpression condition;
    private final Optional<IntLiteral> maxRepetitions;
    private final Block body;

    
    private WhileLoop(NativeExpression condition, Optional<IntLiteral> maxRepetitions, Block body) {
        this.condition = condition;
        this.maxRepetitions = maxRepetitions;
        this.body = body;
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
	public boolean canReturn(Optional<BaseType> type) {
		return body.canReturn(type);
	}

	// May completely skip a while loop
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of(body);
	}

	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    public static WhileLoop simple(NativeExpression condition, Block body) {
        return new WhileLoop(condition, Optional.<IntLiteral> absent(), body);
    }
    
    public static WhileLoop limited(
            NativeExpression condition, IntLiteral maxRepetitions, Block body) {
        return new WhileLoop(condition, Optional.of(maxRepetitions), body);
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
