package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class CharLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    
    public CharLiteral(String value) {
        this.value = value;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "'" + value + "'";
    }

    // Characters do not produce or contribute to return statements
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	// Characters do not produce or contribute to return statements
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
