package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public abstract class ControlFlowNode {
	private static long nodeIDGenerator = 0;
	private final long nodeID;
	
	public ControlFlowNode(){
		this.nodeID = nodeIDGenerator++;
	}

    /**
     * Get all the ControlFlowNodes that this node can flow to.
     * 
     * This method will probably only be used for printing Control Flow Graphs,
     * because we will need more specific getters to actually do meaningful
     * computation.
     */
    public Collection<ControlFlowNode> getSinks(){ return ImmutableList.of();};

    /**
     * Some text that represents the contents of this node.
     *
     * <p>When a node is rendered in a graph, this text is printed inside the rendered node.
     */
    public String nodeText(){ return ""; };

    public long getNodeID(){
    	return nodeID;
    }

    @Override
    public int hashCode() {
        return (int) nodeID; 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ControlFlowNode other = (ControlFlowNode) obj;
        if (nodeID != other.nodeID)
            return false;
        return true;
    }
}
