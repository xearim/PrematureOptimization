package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;

public class ContinueStatementGraphFactory implements ControlTerminalGraphFactory {

    public ContinueStatementGraphFactory() {}

    private ControlTerminalGraph calculateGraph() {
        SequentialControlFlowNode start = SequentialControlFlowNode.namedNop("CSGF start");
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("CSGF end");
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("CSGF cont");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("CSGF break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("CSGF return");

        start.setNext(continueNode);

        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode,continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return calculateGraph();
    }

}
