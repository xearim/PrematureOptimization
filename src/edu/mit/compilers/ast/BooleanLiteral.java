package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BooleanLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    
    public BooleanLiteral(String value) {
        checkArgument(value == "true" || value == "false");
        this.value = value;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    // booleans can only return booleans
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(type.isPresent() && type.get() == BaseType.BOOLEAN)
			return true;
		return false;
	}

	// booleans must only return booleans
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		if(type.isPresent() && type.get() == BaseType.BOOLEAN)
			return true;
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
