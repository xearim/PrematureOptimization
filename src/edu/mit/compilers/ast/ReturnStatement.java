package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ReturnStatement implements Statement {

    private final Optional<NativeExpression> value;
    
    private ReturnStatement(Optional<NativeExpression> value) {
        this.value = value;
    }
    
    public static ReturnStatement ofVoid() {
        return new ReturnStatement(Optional.<NativeExpression>absent());
    }
    
    public static ReturnStatement of(NativeExpression value) {
        return new ReturnStatement(Optional.of(value));
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        if (value.isPresent()) {
            return ImmutableList.of(value.get());
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public String getName() {
        return "return";
    }

    // A return statement always gives a single, definitive return type equal to that of its values evaluation
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		if(!type.isPresent() && !value.isPresent())
			return true;
		return value.get().evalType().equals(type);
	}

	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		if(!type.isPresent() && !value.isPresent())
			return true;
		return value.get().evalType().equals(type);
	}

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	// However, returns do not evaluate to anything themselves (means return return false is wrong)
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
