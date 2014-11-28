package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;

public class DataFlowIntRep {
    private final BcrFlowGraph<ScopedStatement> dataFlowGraph;
    private final Scope scope;

    public DataFlowIntRep(BcrFlowGraph<ScopedStatement> dataFlowGraph, Scope scope) {
        this.dataFlowGraph = dataFlowGraph;
        this.scope = scope;
    }

    public BcrFlowGraph<ScopedStatement> getDataFlowGraph() {
        return dataFlowGraph;
    }

    public Scope getScope() {
        return scope;
    }
}
