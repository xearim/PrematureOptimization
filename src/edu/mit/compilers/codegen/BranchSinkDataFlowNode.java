package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.HashMap;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class BranchSinkDataFlowNode extends DataFlowNode{
	
	private String name;
	private HashMap<Long, DataFlowNode> prev;
	private Optional<DataFlowNode> next;
	
	public BranchSinkDataFlowNode(String name){
		super();
		this.name = name;
		this.prev = new HashMap<Long, DataFlowNode>();
		this.next = Optional.<DataFlowNode>absent();
	}
	
	public BranchSinkDataFlowNode(){
		this("sink");
	}
	
	public boolean hasNext() {
        return next.isPresent();
    }
    
    public boolean hasPrev() {
        return !prev.isEmpty();
    }
    
    public boolean prevContains(DataFlowNode node){
    	return this.prev.containsKey(node.getNodeID());
    }

    public DataFlowNode getNext() {
    	return next.get();
    }
    
    public Collection<DataFlowNode> getPrev() {
        return prev.values();
    }
    
    public void setNext(DataFlowNode next) {
        Preconditions.checkState(!this.equals(next));
        this.next = Optional.of(next);
    }
    
    public void setPrev(DataFlowNode prev) {
        Preconditions.checkState(!this.equals(prev));
        this.prev.put(prev.getNodeID(), prev);
    }
    
    public void clearNext() {
        this.next = Optional.absent();
    }
    
    public void removeFromPrev(DataFlowNode prev) {
    	Preconditions.checkState(this.prev.containsKey(prev.getNodeID()));
    	this.prev.remove(prev.getNodeID());
    }
    
    public void clearPrev() {
        this.prev = new HashMap<Long, DataFlowNode>();
    }

    @Override
    public Collection<DataFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<DataFlowNode>of(getNext())
                : ImmutableList.<DataFlowNode>of();
    }
    
    @Override
    public String nodeText() {
        return name;
    }
}
