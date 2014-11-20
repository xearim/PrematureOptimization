package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.FlowGraph;

public class DataFlowIntRep {
    private final FlowGraph<ScopedStatement> dataFlowGraph;
    private final Scope scope;

    public DataFlowIntRep(FlowGraph<ScopedStatement> dataFlowGraph, Scope scope) {
        this.dataFlowGraph = dataFlowGraph;
        this.scope = scope;
    }

    public FlowGraph<ScopedStatement> getDataFlowGraph() {
        return dataFlowGraph;
    }

    public Scope getScope() {
        return scope;
    }
}
