package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class IfStatement implements Statement {
    private final NativeExpression condition;
    private final Block thenBlock;
    private final Optional<Block> elseBlock;
    private final LocationDescriptor locationDescriptor;

    private IfStatement(NativeExpression condition, Block thenBlock,
            Optional<Block> elseBlock, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.locationDescriptor = locationDescriptor;
    }

    public static IfStatement ifThen(NativeExpression condition, Block thenBlock,
            LocationDescriptor locationDescriptor) {
        return new IfStatement(
                condition, thenBlock, Optional.<Block> absent(), locationDescriptor);
    }

    public static IfStatement ifThenElse(NativeExpression condition, Block thenBlock,
            Block elseBlock, LocationDescriptor locationDescriptor) {
        return new IfStatement(condition, thenBlock, Optional.of(elseBlock), locationDescriptor);
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        builder.add(condition);
        builder.add(thenBlock);
        if (elseBlock.isPresent()) {
            builder.add(elseBlock.get());
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return "if" + (elseBlock.isPresent() ? "-else" : "");
    }

    @Override
    public Iterable<Block> getBlocks() {
        if(elseBlock.isPresent())
            return ImmutableList.of(thenBlock, elseBlock.get());
        return ImmutableList.of(thenBlock);
    }

    @Override
    public boolean canReturn() {
        for(Statement subStatement: thenBlock.getStatements()){
            if(subStatement.canReturn())
                return true;
        }
        if(elseBlock.isPresent())
            for(Statement subStatement: elseBlock.get().getStatements()){
                if(subStatement.canReturn())
                    return true;
            }
        return false;
    }
    
    @Override
	public long getMemorySize() {
    	if(elseBlock.isPresent())
    		return thenBlock.getMemorySize() > elseBlock.get().getMemorySize()
    					? thenBlock.getMemorySize()
    					: elseBlock.get().getMemorySize();
    	else
    		return thenBlock.getMemorySize();
	}

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getCondition() {
        return condition;
    }

    public Block getThenBlock() {
        return thenBlock;
    }

    public Optional<Block> getElseBlock() {
        return elseBlock;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result
                + ((elseBlock == null) ? 0 : elseBlock.hashCode());
        result = prime * result
                + ((thenBlock == null) ? 0 : thenBlock.hashCode());
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
        if (!(obj instanceof IfStatement)) {
            return false;
        }
        IfStatement other = (IfStatement) obj;
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (elseBlock == null) {
            if (other.elseBlock != null) {
                return false;
            }
        } else if (!elseBlock.equals(other.elseBlock)) {
            return false;
        }
        if (thenBlock == null) {
            if (other.thenBlock != null) {
                return false;
            }
        } else if (!thenBlock.equals(other.thenBlock)) {
            return false;
        }
        return true;
    }
}
