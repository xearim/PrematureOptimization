package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ScalarLocation implements Location {

    private final String variableName;
    
    public ScalarLocation(String variableName) {
        this.variableName = variableName;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName;
    }

    // Scalars dont return on their own
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	// Scalars must be looked up in scope to have a meaningful evaluation type
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
