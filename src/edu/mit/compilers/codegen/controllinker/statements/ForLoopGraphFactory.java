package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.increment;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.BlockGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.codegen.controllinker.VariableLoadGraphFactory;

public class ForLoopGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public ForLoopGraphFactory(ForLoop forLoop, Scope scope) {
        this.graph = calculateGraph(forLoop, scope);
    }

    private ControlTerminalGraph calculateGraph(ForLoop forLoop, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.namedNop("FL start");
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("FL end");
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("FL cont");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("FL break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("FL return");
        SequentialControlFlowNode loopStart = SequentialControlFlowNode.namedNop("FL loopStart");

        VariableReference loopVar = new VariableReference(forLoop.getLoopVariable().getName(),
                scope);

        BiTerminalGraph minAssigner = BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(forLoop.getRangeStart(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10),
                        move(Register.R10, loopVar)));

        // TODO(jasonpr): Only evaluate the loop end expression once.
        BiTerminalGraph loopComparator = BiTerminalGraph.sequenceOf(
                new VariableLoadGraphFactory(forLoop.getLoopVariable(), scope).getGraph(),
                new NativeExprGraphFactory(forLoop.getRangeEnd(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10), // Range end.
                        pop(Register.R11), // Loop variable.
                        compareFlagged(Register.R11, Register.R10)));

        BiTerminalGraph incrementor = BiTerminalGraph.ofInstructions(
                move(loopVar, Register.R10),
                increment(Register.R10),
                move(Register.R10, loopVar));

        ControlTerminalGraph body = new BlockGraphFactory(forLoop.getBody()).getGraph();

        BranchingControlFlowNode branch = new BranchingControlFlowNode(JumpType.JL, body.getBeginning(), end);

        // All the nodes have been made.  Make the connections.
        start.setNext(minAssigner.getBeginning());
        minAssigner.getEnd().setNext(loopStart);
        loopStart.setNext(loopComparator.getBeginning());
        loopComparator.getEnd().setNext(branch);
        body.getEnd().setNext(incrementor.getBeginning());
        incrementor.getEnd().setNext(loopStart);
        body.getControlNodes().getBreakNode().setNext(end);
        body.getControlNodes().getContinueNode().setNext(incrementor.getBeginning());
        body.getControlNodes().getReturnNode().setNext(returnNode);

        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}
