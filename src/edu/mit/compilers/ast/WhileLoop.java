package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.Architecture;

public class WhileLoop implements Statement {
    private final NativeExpression condition;
    private final Optional<IntLiteral> maxRepetitions;
    private final Block body;
    private final LocationDescriptor locationDescriptor;

    private WhileLoop(NativeExpression condition, Optional<IntLiteral> maxRepetitions,
            Block body, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.maxRepetitions = maxRepetitions;
        this.body = body;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        builder.add(condition);
        if (maxRepetitions.isPresent()) {
            builder.add(maxRepetitions.get());
        }
        builder.add(body);
        return builder.build();
    }

    @Override
    public String getName() {
        return "while";
    }

	@Override
	public Iterable<Block> getBlocks() {
		return ImmutableList.of(body);
	}

	@Override
	public boolean canReturn() {
		for(Statement subStatement: body.getStatements()){
			if(subStatement.canReturn())
				return true;
		}
		return false;
	}
	
	@Override
	public long getMemorySize() {
		return body.getMemorySize() + Architecture.WHILE_LOOP_VAR_SIZE;
	}

    public static WhileLoop
	simple(NativeExpression condition, Block body, LocationDescriptor locationDescriptor) {
        return new WhileLoop(condition, Optional.<IntLiteral> absent(), body, locationDescriptor);
    }
    
    public static WhileLoop limited(NativeExpression condition, IntLiteral maxRepetitions,
            Block body, LocationDescriptor locationDescriptor) {
        return new WhileLoop(condition, Optional.of(maxRepetitions), body, locationDescriptor);
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getCondition() {
        return condition;
    }

    public Optional<IntLiteral> getMaxRepetitions() {
        return maxRepetitions;
    }
    
    public Block getBody() {
    	return body;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result
                + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result
                + ((maxRepetitions == null) ? 0 : maxRepetitions.hashCode());
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
        if (!(obj instanceof WhileLoop)) {
            return false;
        }
        WhileLoop other = (WhileLoop) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (maxRepetitions == null) {
            if (other.maxRepetitions != null) {
                return false;
            }
        } else if (!maxRepetitions.equals(other.maxRepetitions)) {
            return false;
        }
        return true;
    }
}
