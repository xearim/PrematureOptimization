package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;

public class BlockGraphFactory implements ControlTerminalGraphFactory {
//
//    private final Block block;
//    private final Scope scope;
    private final ControlTerminalGraph graph;
//    private final boolean inLoop;

    public BlockGraphFactory(Block block, Scope scope, boolean inLoop) {
//        this.block = block;
//        this.scope = scope;
//        this.inLoop = inLoop;
        
        this.graph = calculateGraph(block, scope);
    }

    private ControlTerminalGraph calculateGraph(Block block, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal(); 
        
        // TODO(xearim): zero out all the values of the node
        throw new RuntimeException("Need to zero out all values in block graph factory");
        
        SequentialControlFlowNode currentNode = start;
        for (Statement statement : block.getStatements()) {
//            if (statement instanceof )
            ControlTerminalGraph statementGraph = 
                    new StatementGraphFactory(statement, scope).getGraph();
            
            // Hook up control flow nodes
            statementGraph.getControlNodes().getContinueNode().setNext(continueNode);
            statementGraph.getControlNodes().getBreakNode().setNext(breakNode);
            statementGraph.getControlNodes().getReturnNode().setNext(returnNode);
            
            // Hook up statement graph to previous
            currentNode.setNext(statementGraph.getBeginning());
            currentNode = statementGraph.getEnd();
        }
        
        currentNode.setNext(end);
        
        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}
