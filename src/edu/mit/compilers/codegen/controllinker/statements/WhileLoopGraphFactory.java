package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.increment;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.BlockGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class WhileLoopGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public WhileLoopGraphFactory(WhileLoop whileLoop, Scope scope) {
        this.graph = calculateGraph(whileLoop,scope);
    }

    private ControlTerminalGraph calculateGraph(WhileLoop whileLoop, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode loopStart = SequentialControlFlowNode.nopTerminal();

    	// Assign the max Rep var
        BiTerminalGraph maxRepetitionsAssigner = BiTerminalGraph.ofInstructions(
        											push(Literal.INITIAL_VALUE)
        											);
        // Remove the max Rep var
        BiTerminalGraph maxRepetitionsEndDestroyer = BiTerminalGraph.ofInstructions(
				pop(Register.R10) // pop our max rep var off the stack
				);

        // Comparisons for the loop
        BiTerminalGraph loopComparator = BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(whileLoop.getCondition(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10), // Evaluated conditional.
                        move(Literal.TRUE, Register.R11), // true.
                        compareFlagged(Register.R10, Register.R11)));

        // increment the max Repetitions var towards maxRepetitions
        BiTerminalGraph incrementor = BiTerminalGraph.ofInstructions(
                pop(Register.R10),
                increment(Register.R10),
                push(Register.R10));

        // The actual execution body
        ControlTerminalGraph body = new BlockGraphFactory(whileLoop.getBody()).getGraph();

        // The branch for checking conditionals
        BranchingControlFlowNode conditionalBranch = new BranchingControlFlowNode(JumpType.JNE,
        									body.getBeginning(), maxRepetitionsEndDestroyer.getBeginning());

        BiTerminalGraph maxRepetitionsReturnDestroyer = BiTerminalGraph.ofInstructions(
                // Save return value in R10.
                pop(Register.R10),
                // Pop the repetition count into R11, and ignore it.
                pop(Register.R11),
                // Push the return value back onto the stack.
                push(Register.R10));

        BiTerminalGraph maxRepetitionsComparator = BiTerminalGraph.ofInstructions(
                move(new Location(Register.RSP, 1*Architecture.BYTES_PER_ENTRY), Register.R10), // move max repetitions counter to R10
                move(new Literal(whileLoop.getMaxRepetitions().get().get64BitValue()), Register.R11), // max repetitions
                compareFlagged(Register.R10, Register.R11));

        // The branch for checking repetition number
        ControlFlowNode maxRepetitionBranch;

        if (whileLoop.getMaxRepetitions().isPresent()) {
            maxRepetitionBranch = new BranchingControlFlowNode(JumpType.JGE,
                    loopComparator.getBeginning(), maxRepetitionsEndDestroyer.getBeginning());
        } else {
            // Don't actually branch!  We don't have a max repetitions value,
            // so just go to the loopComparator.
            maxRepetitionBranch = SequentialControlFlowNode.nopWithNext(
                    loopComparator.getBeginning(), "No max nop");
        }

        // All the nodes have been made.  Make the connections.
        start.setNext(maxRepetitionsAssigner.getBeginning());
        maxRepetitionsAssigner.getEnd().setNext(loopStart);
        loopStart.setNext(maxRepetitionsComparator.getBeginning());
        maxRepetitionsComparator.getEnd().setNext(maxRepetitionBranch);

        loopComparator.getEnd().setNext(conditionalBranch);
        body.getEnd().setNext(incrementor.getBeginning());
        incrementor.getEnd().setNext(loopStart);
        body.getControlNodes().getBreakNode().setNext(maxRepetitionsEndDestroyer.getBeginning());
        body.getControlNodes().getContinueNode().setNext(incrementor.getBeginning());
        body.getControlNodes().getReturnNode().setNext(maxRepetitionsReturnDestroyer.getBeginning());
        maxRepetitionsReturnDestroyer.getEnd().setNext(returnNode);

        return new ControlTerminalGraph(start, end,
                	new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
