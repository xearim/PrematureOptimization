package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class BranchGraphFactory implements GraphFactory {

    private final TerminaledGraph graph;

    public BranchGraphFactory(TerminaledGraph condition, TerminaledGraph trueBranch,
            TerminaledGraph falseBranch) {
        this.graph = constructGraph(condition, trueBranch, falseBranch);
    }

    private TerminaledGraph constructGraph(TerminaledGraph condition, TerminaledGraph trueBranch,
            TerminaledGraph falseBranch) {

        // When we have the boolean result of a condition on the stack, we must execute these
        // instructions to be able to jump in response to its value.
        TerminaledGraph conditionEvaluator = TerminaledGraph.ofInstructions(
                pop(R10),
                compareFlagged(R10, Literal.TRUE));

        // Setup the branching node.
        BranchingControlFlowNode branch = new BranchingControlFlowNode(
                JumpType.JNE,
                trueBranch.getBeginning(),
                falseBranch.getBeginning());

        // Let the two branches merge back to this sink.
        SequentialControlFlowNode sink = SequentialControlFlowNode.nopTerminal();
        trueBranch.getEnd().setNext(sink);
        falseBranch.getEnd().setNext(sink);

        return TerminaledGraph.sequenceOf(
                condition,
                conditionEvaluator,
                new TerminaledGraph(branch, sink));
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }
}
