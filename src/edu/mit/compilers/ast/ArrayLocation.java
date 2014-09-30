package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ArrayLocation implements Location {

    private final String variableName;
    private final int index;
    
    public ArrayLocation(String variableName, int index) {
        this.variableName = variableName;
        this.index = index;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return variableName + "[" + index +"]";
    }

    // ArrayLocations are effectively integers only
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(type.isPresent() && type.get() == BaseType.INTEGER)
			return true;
		return false;
	}

	// ArrayLocations must return integers 
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		if(type.isPresent() && type.get() == BaseType.INTEGER)
			return true;
		return false;
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
