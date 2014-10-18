package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;

public class ContinueStatementGraphFactory implements ControlTerminalGraphFactory {
    private ControlTerminalGraph graph;

    public ContinueStatementGraphFactory() {
        this.graph = calculateGraph();
    }

    private ControlTerminalGraph calculateGraph() {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal();

        start.setNext(continueNode);

        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode,continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
