package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class Assignment implements Statement {
    private final Location location;
    private final AssignmentOperation operation;
    private final NativeExpression expression;
    private final LocationDescriptor locationDescriptor;
    
    private final boolean fromCompiler;
    
    public Assignment(Location location, AssignmentOperation operation,
            NativeExpression expression, LocationDescriptor locationDescriptor, boolean fromCompiler) {
        this.location = location;
        this.operation = operation;
        this.expression = expression;
        this.locationDescriptor = locationDescriptor;
        this.fromCompiler = fromCompiler;
    }
    
    public Assignment(Location location, AssignmentOperation operation,
            NativeExpression expression, LocationDescriptor locationDescriptor) {
        this(location, operation, expression, locationDescriptor, false);
    }
    
    public static Assignment compilerAssignment(Location location, NativeExpression expr){
    	return new Assignment(location, AssignmentOperation.SET_EQUALS,
    						  expr, LocationDescriptor.machineCode(), true);
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
    
    public boolean getFromCompiler() {
    	return fromCompiler;
    }
    
    public String asText() {
    	return location.asText() + " " + operation.getSymbol() + " " + expression.asText();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result
                + ((operation == null) ? 0 : operation.hashCode());
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
        if (!(obj instanceof Assignment)) {
            return false;
        }
        Assignment other = (Assignment) obj;
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (operation != other.operation) {
            return false;
        }
        return true;
    }
}
