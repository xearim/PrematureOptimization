package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ContinueStatement implements Statement {

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "continue";
    }

    // Continue does not produce a return value
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	// Continue does not produce a return value
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
}
