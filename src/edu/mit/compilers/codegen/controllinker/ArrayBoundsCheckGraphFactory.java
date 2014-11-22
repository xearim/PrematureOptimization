package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class ArrayBoundsCheckGraphFactory implements GraphFactory {

    private final IntLiteral arrayLength;

    public ArrayBoundsCheckGraphFactory(IntLiteral arrayLength) {
        this.arrayLength = arrayLength;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();

        // Set R10 to the requested index, and compare them.
        builder.append(pop(Register.R10))
                .append(compareFlagged(Register.R10, new Literal(arrayLength)));

        // Exit if the index is too high.
        Node<Instruction> upperBoundBranch = Node.nop();
        FlowGraph<Instruction> errorExit =
                new ErrorExitGraphFactory(Literal.ARRAY_OUT_OF_BOUNDS_EXIT).getGraph();
        Node<Instruction> lowerBoundComparator =
                Node.of(compareFlagged(Register.R10, new Literal(0)));
        builder.append(upperBoundBranch)
                .linkNonJumpBranch(upperBoundBranch, lowerBoundComparator)
                .setEnd(lowerBoundComparator)
                .linkJumpBranch(upperBoundBranch, JumpType.JGE, errorExit);

        // Exit if the index is too low.  Otherwise, push the valid index back
        // onto the stack.
        Node<Instruction> lowerBoundBranch = Node.nop();
        Node<Instruction> validPusher = Node.of(push(Register.R10));
        builder.append(lowerBoundBranch)
                .linkNonJumpBranch(lowerBoundBranch, validPusher)
                .linkJumpBranch(lowerBoundBranch, JumpType.JL, errorExit.getStart())
                .setEnd(validPusher);

        return builder.build();
    }
}
