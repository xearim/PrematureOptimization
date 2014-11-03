package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class BranchSourceDataFlowNode extends DataFlowNode{
	
	private String name;
	private JumpType type;
	private Optional<SequentialDataFlowNode> prev;
	private SequentialDataFlowNode trueBranch, falseBranch;
	
	public BranchSourceDataFlowNode(JumpType type, String name){
		super();
		this.type = type;
		this.prev = Optional.<SequentialDataFlowNode>absent();
		this.trueBranch = SequentialDataFlowNode.nop();
		this.falseBranch = SequentialDataFlowNode.nop();
	}
	
	public BranchSourceDataFlowNode(JumpType type){
		this(type, "branch");
	}
	
	public boolean hasNext() {
        return true;
    }
    
    public boolean hasPrev() {
        return prev.isPresent();
    }
    
    public JumpType getType() {
    	return type;
    }

    public SequentialDataFlowNode getTrueBranch() {
        return trueBranch;
    }
    
    public SequentialDataFlowNode getFalseBranch() {
        return falseBranch;
    }
    
    public SequentialDataFlowNode getPrev() {
        return prev.get();
    }
    
    public void setPrev(SequentialDataFlowNode prev) {
        Preconditions.checkState(!this.equals(prev));
        this.prev = Optional.of(prev);
    }
    
    public void clearPrev() {
        this.prev = Optional.absent();
    }

    @Override
    public Collection<DataFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<DataFlowNode>of(getTrueBranch(), getFalseBranch())
                : ImmutableList.<DataFlowNode>of();
    }
    
    @Override
    public String nodeText() {
        return name;
    }

}
