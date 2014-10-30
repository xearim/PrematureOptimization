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
	
	@Override
	public long getMemorySize() {
		return 0;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public Optional<NativeExpression> getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ReturnStatement)) {
            return false;
        }
        ReturnStatement other = (ReturnStatement) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
