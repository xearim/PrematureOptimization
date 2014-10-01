package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ForLoop implements Statement {

    private final ScalarLocation loopVariable;
    private final NativeExpression rangeStart;
    private final NativeExpression rangeEnd;
    private final Block body;
    
    public ForLoop(ScalarLocation loopVariable, NativeExpression rangeStart,
            NativeExpression rangeEnd, Block body) {
        this.loopVariable = loopVariable;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.body = body;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(loopVariable, rangeStart, rangeEnd, body);
    }

    @Override
    public String getName() {
        return "for";
    }

    // A For loop produces returns equivalent to its contained block element
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return body.canReturn(type);
	}

	// May entirely skip a for block
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of(body);
	}

	// For blocks don't evaluate
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
