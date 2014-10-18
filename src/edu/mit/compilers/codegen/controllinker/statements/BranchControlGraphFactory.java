package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;

public class BranchControlGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    /*
     * Assume that condition pushes the result of a boolean evaluation to the
     * stack.
     */
    public BranchControlGraphFactory(BiTerminalGraph condition,
            ControlTerminalGraph thenBlock, ControlTerminalGraph elseBlock) {
        this.graph = calculateGraph(condition,thenBlock, elseBlock);
    }

    private ControlTerminalGraph calculateGraph(BiTerminalGraph condition,
            ControlTerminalGraph thenBlock, ControlTerminalGraph elseBlock) {
        // TODO(xearim): create a ControlTerminalGraph from these parameters
        // Don't forget continue, break, and return nodes
        throw new RuntimeException("Don't know how to create graph for BranchControlGraphFactory");
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
