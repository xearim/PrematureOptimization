package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.dataflow.DataFlow;

public class DataFlowIntRep {
    private final DataFlow dataFlowGraph;
    private final Scope scope;
    
    public DataFlowIntRep(DataFlow dataFlowGraph, Scope scope) {
        this.dataFlowGraph = dataFlowGraph;
        this.scope = scope;
    }

    public DataFlow getDataFlowGraph() {
        return dataFlowGraph;
    }

    public Scope getScope() {
        return scope;
    }
}
