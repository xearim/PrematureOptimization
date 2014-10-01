package edu.mit.compilers.ast;

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

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	@Override
	public boolean canReturn() {
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
}
