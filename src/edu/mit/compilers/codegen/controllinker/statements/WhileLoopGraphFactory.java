package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;

public class WhileLoopGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public WhileLoopGraphFactory(WhileLoop wl, Scope scope) {
        this.graph = calculateGraph(wl,scope);
    }

    private ControlTerminalGraph calculateGraph(WhileLoop wl, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal(); 

        /*
         * TODO(xearim):
         * 1) conditional
         * 2) zero out all the values of the node
         * 3) Hook up the continue, and break nodes for the block correctly
         * 4) uncomment return statement
         */
        throw new RuntimeException("For loop graph factory unimplemented");

        //return new ControlTerminalGraph(start, end,
        //        new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
