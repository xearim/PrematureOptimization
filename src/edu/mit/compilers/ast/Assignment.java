package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class Assignment implements Statement {
    private final Location location;
    private final AssignmentOperation operation;
    private final NativeExpression expression;
    private final LocationDescriptor locationDescriptor;
    
    public Assignment(Location location, AssignmentOperation operation,
            NativeExpression expression, LocationDescriptor locationDescriptor) {
        this.location = location;
        this.operation = operation;
        this.expression = expression;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(location, expression);
    }

    @Override
    public String getName() {
        return operation.getSymbol();
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

    public Location getLocation() {
        return location;
    }

    public AssignmentOperation getOperation() {
        return operation;
    }

    public NativeExpression getExpression() {
        return expression;
    }
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
