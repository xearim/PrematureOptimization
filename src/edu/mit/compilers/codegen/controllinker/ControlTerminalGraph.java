package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;


/**
 * A control flow graph with a a beginning node and and end node, and three special
 * control flow nodes.
 *
 * <p>Like BiTerminalGraph, this graph has one beginning node and one end node.  Unlike
 * BiTerminalGraph, it has three alternate end nodes: a "break", a "continue", and a
 * "return" node.
 *
 */
public class ControlTerminalGraph {
    private final ControlFlowNode beginning;
    private final SequentialControlFlowNode end;
    private final ControlNodes controlNodes;

    public static class ControlNodes {
        private final SequentialControlFlowNode breakNode;
        private final SequentialControlFlowNode continueNode;
        private final SequentialControlFlowNode returnNode;

        public ControlNodes(SequentialControlFlowNode breakNode,
                SequentialControlFlowNode continueNode, SequentialControlFlowNode returnNode) {
            this.breakNode = breakNode;
            this.continueNode = continueNode;
            this.returnNode = returnNode;
        }

        public SequentialControlFlowNode getBreakNode() {
            return breakNode;
        }

        public SequentialControlFlowNode getContinueNode() {
            return continueNode;
        }

        public SequentialControlFlowNode getReturnNode() {
            return returnNode;
        }
    }

    public ControlTerminalGraph(
            ControlFlowNode beginning, SequentialControlFlowNode end,ControlNodes controlNodes) {
        this.beginning = beginning;
        this.end = end;
        this.controlNodes = controlNodes;
    }

    public static ControlTerminalGraph ofBiTerminalGraph(BiTerminalGraph biTerminal) {
        return new ControlTerminalGraph(biTerminal.getBeginning(), biTerminal.getEnd(),
                new ControlNodes(SequentialControlFlowNode.nopTerminal(),
                        SequentialControlFlowNode.nopTerminal(),
                        SequentialControlFlowNode.nopTerminal()));
    }

    public ControlFlowNode getBeginning() {
        return beginning;
    }

    public SequentialControlFlowNode getEnd() {
        return end;
    }

    public ControlNodes getControlNodes() {
        return controlNodes;
    }

    public static ControlTerminalGraph nopTerminal() {
        // TODO(xearim): implement ControlTerminalGraph equivalent of
        // SequentialTerminalGraph#nopTerminal
        throw new RuntimeException("ControlTerminalGraph#nopTerminal not yet implemented");
    }
}
