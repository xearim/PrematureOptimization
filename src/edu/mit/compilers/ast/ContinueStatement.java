package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ContinueStatement implements Statement {

    private final LocationDescriptor locationDescriptor;

    public ContinueStatement(LocationDescriptor locationDescriptor) {
        this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "continue";
    }

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	@Override
	public boolean canReturn() {
		return false;
	}
	
	@Override
	public long getMemorySize() {
		return 0;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    // All ContinueStatements are equal, since Node specifies that we ignore the
    // LocationDescriptor.
    @Override
    public int hashCode() {
        return 0;
    }

    // All ContinueStatements are equal, since Node specifies that we ignore the
    // LocationDescriptor.
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ContinueStatement;
    }
}
