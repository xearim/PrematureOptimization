package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.Architecture;

public class ForLoop implements Statement {

    private final ScalarLocation loopVariable;
    private final NativeExpression rangeStart;
    private final NativeExpression rangeEnd;
    private final Block body;
    private final LocationDescriptor locationDescriptor;

    public ForLoop(ScalarLocation loopVariable, NativeExpression rangeStart,
		   NativeExpression rangeEnd, Block body, LocationDescriptor locationDescriptor) {
        this.loopVariable = loopVariable;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.body = body;
        this.locationDescriptor = locationDescriptor;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(loopVariable, rangeStart, rangeEnd, body);
    }

    @Override
    public String getName() {
        return "for";
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
		return body.getMemorySize() + Architecture.FOR_LOOP_VAR_SIZE;
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public ScalarLocation getLoopVariable() {
        return loopVariable;
    }

    public NativeExpression getRangeStart() {
        return rangeStart;
    }

    public NativeExpression getRangeEnd() {
        return rangeEnd;
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
                + ((loopVariable == null) ? 0 : loopVariable.hashCode());
        result = prime * result
                + ((rangeEnd == null) ? 0 : rangeEnd.hashCode());
        result = prime * result
                + ((rangeStart == null) ? 0 : rangeStart.hashCode());
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
        if (!(obj instanceof ForLoop)) {
            return false;
        }
        ForLoop other = (ForLoop) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (loopVariable == null) {
            if (other.loopVariable != null) {
                return false;
            }
        } else if (!loopVariable.equals(other.loopVariable)) {
            return false;
        }
        if (rangeEnd == null) {
            if (other.rangeEnd != null) {
                return false;
            }
        } else if (!rangeEnd.equals(other.rangeEnd)) {
            return false;
        }
        if (rangeStart == null) {
            if (other.rangeStart != null) {
                return false;
            }
        } else if (!rangeStart.equals(other.rangeStart)) {
            return false;
        }
        return true;
    }
}
