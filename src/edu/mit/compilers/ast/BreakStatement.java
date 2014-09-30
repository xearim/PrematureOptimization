package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BreakStatement implements Statement {

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "break";
    }

    // Break statements do not produce return values
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	// Break statements do not produce return values
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
}
