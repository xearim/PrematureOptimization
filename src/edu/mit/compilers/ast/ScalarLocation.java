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

    // Scalars can be either Integers or Booleans
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(type.isPresent())
			return true;
		return false;
	}

	// Scalars must be looked up in scope to have a meaningful return value
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
