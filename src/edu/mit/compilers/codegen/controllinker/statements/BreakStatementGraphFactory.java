package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;

public class BreakStatementGraphFactory implements ControlTerminalGraphFactory {

    public BreakStatementGraphFactory() {}
    
    private ControlTerminalGraph calculateGraph() {
        SequentialControlFlowNode start = SequentialControlFlowNode.namedNop("BS start");
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("BS end");
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("BS cont");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("BS break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("BS return");
        
        start.setNext(breakNode);

        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode,continueNode, returnNode));
    }
    
    @Override
    public ControlTerminalGraph getGraph() {
        return calculateGraph();
    }

}
