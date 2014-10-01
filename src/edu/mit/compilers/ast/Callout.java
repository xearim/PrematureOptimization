package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Callout implements Node {

    private final String name;
    
    public Callout(String name) {
        this.name = name;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return name;
    }

    // TODO(xearim): Verify that callouts do not produce returns
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	// Callouts do not evaluate to anything
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
