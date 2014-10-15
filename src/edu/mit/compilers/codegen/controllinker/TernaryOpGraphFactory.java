package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;

public class TernaryOpGraphFactory implements GraphFactory {

    private final TerminaledGraph graph;
    
    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope) {
        this.graph = calculateGraph(operation, scope);
    }

    private TerminaledGraph calculateGraph(TernaryOperation operation, Scope scope) {
        return new BranchGraphFactory(
                new NativeExprGraphFactory(operation.getCondition(), scope).getGraph(),
                new NativeExprGraphFactory(operation.getTrueResult(), scope).getGraph(),
                new NativeExprGraphFactory(operation.getFalseResult(), scope).getGraph())
                .getGraph();
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }
}
