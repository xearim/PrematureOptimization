package edu.mit.compilers.codegen;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;

public interface DataFlowNode {
    public Set<DataFlowNode> getPredecessors();
    public Set<DataFlowNode> getSuccessors();
    public Collection<GeneralExpression> getExpressions();
}
