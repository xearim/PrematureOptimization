package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ReturnStatement implements Statement {

    private final Optional<NativeExpression> value;
    private final LocationDescriptor locationDescriptor;

    private ReturnStatement(Optional<NativeExpression> value, LocationDescriptor locationDescriptor) {
        this.value = value;
	this.locationDescriptor = locationDescriptor;
    }
    
    public static ReturnStatement ofVoid(LocationDescriptor locationDescriptor) {
        return new ReturnStatement(Optional.<NativeExpression>absent(), locationDescriptor);
    }
    
    public static ReturnStatement
            of(NativeExpression value, LocationDescriptor locationDescriptor) {
        return new ReturnStatement(Optional.of(value), locationDescriptor);
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

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of();
	}

	@Override
	public boolean canReturn() {
		return true;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
