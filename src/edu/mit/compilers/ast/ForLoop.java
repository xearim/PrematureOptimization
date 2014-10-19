package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ForLoop implements Statement {
	private static long forLoopIDGenerator = 0;

    private final ScalarLocation loopVariable;
    private final NativeExpression rangeStart;
    private final NativeExpression rangeEnd;
    private final Block body;
    private final LocationDescriptor locationDescriptor;
    private final long forLoopID;

    public ForLoop(ScalarLocation loopVariable, NativeExpression rangeStart,
		   NativeExpression rangeEnd, Block body, LocationDescriptor locationDescriptor) {
        this.loopVariable = loopVariable;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.body = body;
        this.locationDescriptor = locationDescriptor;
        this.forLoopID = forLoopIDGenerator++;
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
		return body.getMemorySize();
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
    
    public String getID() {
    	return Long.toString(forLoopID);
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
}
