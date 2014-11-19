package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class ErrorExitGraphFactory {
    private final Literal exitValue;

    public ErrorExitGraphFactory(Literal exitValue) {
        this.exitValue = exitValue;
    }

    public FlowGraph<Instruction> getGraph() {
        // We need to be able to identify these nodes for the loop.
        Node<Instruction> compareBasePointer = Node.of(compareFlagged(Register.RBP, Register.R10));
        Node<Instruction> cleanMethodScope = Node.of(
                move(new Location(Register.RBP, 0*Architecture.BYTES_PER_ENTRY), Register.RBP));
        Node<Instruction> exitNop = Node.nop();
        
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();
        return builder.append(move(Architecture.MAIN_BASE_POINTER_ERROR_VARIABLE, Register.R10))
                .append(compareBasePointer)
                .linkNonJumpBranch(compareBasePointer, cleanMethodScope)
                .linkJumpBranch(compareBasePointer, JumpType.JE, exitNop)
                .link(cleanMethodScope, compareBasePointer)  // Connect the back edge.
                .setEnd(exitNop)
                .append(move(Register.RBP, Register.RSP))
                .append(pop(Register.RBP))
                .append(move(exitValue, Register.RAX))
                .append(ret())
                .build();
    }
}
