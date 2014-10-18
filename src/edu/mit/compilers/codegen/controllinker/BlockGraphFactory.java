package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.statements.StatementGraphFactory;

public class BlockGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public BlockGraphFactory(Block block) {
        this.graph = calculateGraph(block);
    }

    private ControlTerminalGraph calculateGraph(Block block) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal(); 

        SequentialControlFlowNode currentNode = start;
        for (Statement statement : block.getStatements()) {
            ControlTerminalGraph statementGraph = 
                    new StatementGraphFactory(statement, block.getScope()).getGraph();

            // Hook up control flow nodes
            statementGraph.getControlNodes().getContinueNode().setNext(continueNode);
            statementGraph.getControlNodes().getBreakNode().setNext(breakNode);
            statementGraph.getControlNodes().getReturnNode().setNext(returnNode);

            // Hook up statement graph to previous
            currentNode.setNext(statementGraph.getBeginning());
            currentNode = statementGraph.getEnd();
        }

        currentNode.setNext(end);
        // TODO(xearim): zero out all the values of the node, and bring back
        // return statement
        throw new RuntimeException("Need to zero out all values in block graph factory");

        //return new ControlTerminalGraph(start, end,
        //        new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}
