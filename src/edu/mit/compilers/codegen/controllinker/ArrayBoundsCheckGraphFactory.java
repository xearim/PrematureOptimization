package edu.mit.compilers.codegen.controllinker;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class ArrayBoundsCheckGraphFactory implements GraphFactory {
	
    private final BiTerminalGraph graph;

    public ArrayBoundsCheckGraphFactory(ArrayLocation array, Scope scope) {
        this.graph = constructGraph(array, scope);
    }

    private BiTerminalGraph constructGraph(ArrayLocation array, Scope scope) {
    	// Array should exist
    	checkState(scope.getFromScope(array.getVariableName()).isPresent());
    	FieldDescriptor arrayDescriptor = scope.getFromScope(array.getVariableName()).get();
    	// The field descriptor we get better be an array
    	checkState(arrayDescriptor.getLength().isPresent());
    	
    	// Return our array bounds to the stack
    	SequentialControlFlowNode valid = SequentialControlFlowNode.terminal(push(Register.R10));
    	
    	// Get the proposed array bounds from the stack and put it in R10
    	// Set R11 to the total size of the array minus 1 (aka valid indexes)
    	BiTerminalGraph initialize = BiTerminalGraph.ofInstructions(
    			pop(Register.R10),
    			move(new Literal(arrayDescriptor.getLength().get().get64BitValue() - 1L), Register.R11)
    			);
    	
    	// Check if the array is in bounds
    	BiTerminalGraph compareArrayBounds = BiTerminalGraph.ofInstructions(
    			compareFlagged(Register.R10, Register.R11));
    	
    	// Set up the original bp for the exit operation
    	BiTerminalGraph startExit = BiTerminalGraph.ofInstructions(
    			move(Architecture.MAIN_BASE_POINTER_ERROR_VARIABLE, Register.R10)
    			);
    	
    	BranchingControlFlowNode boundsBranch = new BranchingControlFlowNode(
    			JumpType.JG,
    			valid,
    			startExit.getBeginning()
    			);
    	
    	// If it isnt, we are going to recurse into the old RBP
    	BiTerminalGraph cleanMethodScope = BiTerminalGraph.ofInstructions(
    			move(new Location(Register.RBP, 0*Architecture.BYTES_PER_ENTRY), Register.RBP));
    	
    	// Check if the current RBP is the initial RBP
    	BiTerminalGraph compareBasePointer = BiTerminalGraph.ofInstructions(
    			compareFlagged(Register.RBP, Register.R11));
    	
    	// When we exit, we need to restore RBP and RSP and then put the error code into RAX
    	BiTerminalGraph exit = BiTerminalGraph.ofInstructions(
    			move(Register.RBP, Register.RSP),
    			pop(Register.RBP),
    			move(Literal.ARRAY_OUT_OF_BOUNDS_EXIT, Register.RAX),
    			ret());
    	
    	// We branch between recursively setting RBP and just exiting with our error code
    	BranchingControlFlowNode exitBranch = new BranchingControlFlowNode(
    			JumpType.JE,
    			cleanMethodScope.getBeginning(),
    			exit.getBeginning());
    	
    	// Link all the graph nodes up
    	initialize.getEnd().setNext(compareArrayBounds.getBeginning());
    	compareArrayBounds.getEnd().setNext(boundsBranch);
    	startExit.getEnd().setNext(compareBasePointer.getBeginning());
    	compareBasePointer.getEnd().setNext(exitBranch);
    	cleanMethodScope.getEnd().setNext(startExit.getBeginning());

        return new BiTerminalGraph(initialize.getBeginning(), valid);
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
