package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;

public interface DataFlowNode {
    public Set<DataFlowNode> getPredecessors();
    public Set<DataFlowNode> getSuccessors();
    public Collection<GeneralExpression> getExpressions();
    /**
     * Remove the 'replaced' DFN from the node's predecessors, and put the
     * 'replacement' in its place.
     */
    public void replacePredecessor(DataFlowNode replaced, DataFlowNode replacement);
    /**
     * Remove the 'replaced' DFN from the node's successors, and put the
     * 'replacement' in its place.
     */
    public void replaceSuccessor(DataFlowNode replaced, DataFlowNode replacement);
}
