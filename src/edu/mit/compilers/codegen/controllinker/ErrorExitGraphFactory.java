package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class ErrorExitGraphFactory implements GraphFactory {
    private final BiTerminalGraph graph;

    public ErrorExitGraphFactory(Literal exitValue) {
        this.graph = constructGraph(exitValue);
    }

    private BiTerminalGraph constructGraph(Literal exitValue) {
    	
    	// Set R10 to the global for the intial RBP
    	BiTerminalGraph initialize = BiTerminalGraph.ofInstructions(
    			move(Architecture.MAIN_BASE_POINTER_ERROR_LABEL, Register.R10)
    			);
    	
    	// Check if the current RBP is the initial RBP
    	BiTerminalGraph compareBasePointer = BiTerminalGraph.ofInstructions(
    			compareFlagged(Register.RBP, Register.R10));
    	
    	// If it isnt, we are going to recurse into the old RBP
    	BiTerminalGraph cleanMethodScope = BiTerminalGraph.ofInstructions(
    			move(new Location(Register.RBP, 0*Architecture.BYTES_PER_ENTRY), Register.RBP));
    	
    	// When we exit, we need to restore RBP and RSP and then put the error code into RAX
    	BiTerminalGraph exit = BiTerminalGraph.ofInstructions(
    			move(Register.RBP, Register.RSP),
    			pop(Register.RBP),
    			move(exitValue, Register.RAX),
    			ret());
    	
    	// We branch between recursively setting RBP and just exiting with our error code
    	BranchingControlFlowNode branch = new BranchingControlFlowNode(
    			JumpType.JE,
    			cleanMethodScope.getBeginning(),
    			exit.getBeginning());
    	
    	// Link all the graph nodes up
    	initialize.getEnd().setNext(compareBasePointer.getBeginning());
    	compareBasePointer.getEnd().setNext(branch);
    	cleanMethodScope.getEnd().setNext(compareBasePointer.getBeginning());

        return new BiTerminalGraph(initialize.getBeginning(), exit.getEnd());
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
