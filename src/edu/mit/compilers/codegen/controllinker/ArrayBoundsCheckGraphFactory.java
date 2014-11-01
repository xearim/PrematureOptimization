package edu.mit.compilers.codegen.controllinker;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class ArrayBoundsCheckGraphFactory implements GraphFactory {
    
    private final BiTerminalGraph graph;

    public ArrayBoundsCheckGraphFactory(ArrayLocation array, Scope scope) {
        this.graph = constructGraph(array, scope);
    }

    private BiTerminalGraph constructGraph(ArrayLocation array, Scope scope) {
        // Array should exist
        checkState(scope.getFromScope(array.getVariable()).isPresent());
        FieldDescriptor arrayDescriptor = scope.getFromScope(array.getVariable()).get();
        // The field descriptor we get better be an array
        checkState(arrayDescriptor.getLength().isPresent());
        
        // Return our array bounds to the stack
        SequentialControlFlowNode valid = SequentialControlFlowNode.terminal(push(Register.R10));
        
        // Get the proposed array bounds from the stack and put it in R10
        // Set R11 to the total size of the array minus 1 (aka valid indexes)
        BiTerminalGraph initialize = BiTerminalGraph.ofInstructions(
                pop(Register.R10),
                move(new Literal(arrayDescriptor.getLength().get().get64BitValue()), Register.R11)
                );
        
        // Check if the array is in bounds
        BiTerminalGraph compareArrayUpperBounds = BiTerminalGraph.ofInstructions(
                compareFlagged(Register.R10, Register.R11));
        
        BiTerminalGraph compareArrayLowerBounds = BiTerminalGraph.ofInstructions(
                compareFlagged(Register.R10, new Literal(0)));
        
        // If it isnt, we are going to exit with an error Array out of Bounds
        BiTerminalGraph exit = new ErrorExitGraphFactory(Literal.ARRAY_OUT_OF_BOUNDS_EXIT).getGraph();
        
        BranchingControlFlowNode boundsUpperBranch = new BranchingControlFlowNode(
                JumpType.JGE,
                compareArrayLowerBounds.getBeginning(),
                exit.getBeginning()
                );
        
        BranchingControlFlowNode boundsLowerBranch = new BranchingControlFlowNode(
                JumpType.JL,
                valid,
                exit.getBeginning()
                );
        
        
        
        // Link all the graph nodes up
        initialize.getEnd().setNext(compareArrayUpperBounds.getBeginning());
        compareArrayUpperBounds.getEnd().setNext(boundsUpperBranch);
        compareArrayLowerBounds.getEnd().setNext(boundsLowerBranch);

        return new BiTerminalGraph(initialize.getBeginning(), valid);
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
