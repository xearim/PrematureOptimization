package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveFromMemory;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.movePointer;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveToMemory;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.common.Variable;


public class VariableLoadGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;

    public VariableLoadGraphFactory(Location location, Scope scope, boolean inMethodCall) {
    	// You always need to check a load
        this.graph = calculateLoad(location, scope, false, inMethodCall);
    }
    
    public VariableLoadGraphFactory(Location location, Scope scope){
    	this.graph = calculateLoad(location, scope, false, false);
    }

    private BiTerminalGraph calculateLoad(Location location, Scope scope, boolean check, boolean inMethodCall) {
        if (location instanceof ArrayLocation) {
            return calculateLoadFromArray((ArrayLocation) location, scope, check, inMethodCall);
        } else if (location instanceof ScalarLocation) {
            return calculateLoadFromScalar((ScalarLocation) location, scope, inMethodCall);
        } else {
            throw new AssertionError("Unexpected location type for " + location);
        }
    }

    private static BiTerminalGraph calculateLoadFromArray(ArrayLocation location, Scope scope, boolean check, boolean inMethodCall) {
        return BiTerminalGraph.sequenceOf(
                setupArrayRegisters(location, scope, check, inMethodCall),
                BiTerminalGraph.ofInstructions(
                        moveFromMemory(
                                offset(location, scope), Register.R10, Register.R11,
                                Architecture.WORD_SIZE, Register.R10),
                        push(Register.R10)));
    }
    // TODO(jasonpr): Have this code live somewhere sensible.
    public static BiTerminalGraph calculateStoreToArray(ArrayLocation location, Scope scope, boolean check, boolean inMethodCall) {
        return BiTerminalGraph.sequenceOf(
                BiTerminalGraph.ofInstructions(
                        pop(Register.RAX)), // Pop value to store.
                setupArrayRegisters(location, scope, check, inMethodCall),
                BiTerminalGraph.ofInstructions(
                        moveToMemory(Register.RAX, offset(location, scope), Register.R10,
                                Register.R11, Architecture.WORD_SIZE)));

    }
    public static BiTerminalGraph calculateStoreToArray(ArrayLocation location, Scope scope, boolean check) {
    	return calculateStoreToArray(location, scope, check, false);
    }
    /**
     * Load values into R10, and R11 so that 'X(%r10, %r11, 8)' refers to the array
     * location.
     */
    public static BiTerminalGraph setupArrayRegisters(ArrayLocation location, Scope scope, boolean check, boolean inMethodCall) {
        Scope immediateScope = scope.getLocation(location.getVariable());
        ScopeType scopeType = immediateScope.getScopeType();
        if (scopeType == ScopeType.LOCAL) {
            return !check
            		? BiTerminalGraph.sequenceOf(
            		// The size of the array for bounds checking, we will trash this in the checker
            		BiTerminalGraph.ofInstructions(
            				push(new Literal(scope.getFromScope(location.getVariable()).get().getLength().get().get64BitValue()))
            				),
                    // Locals are offset from stack pointer.
                    new NativeExprGraphFactory(location.getIndex(), scope, inMethodCall).getGraph(),
                    // We are going to borrow the array index and
                    // Check it boundaries against the array size
                    new ArrayBoundsCheckGraphFactory().getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(Register.R11),
                            move(Register.RBP, Register.R10)))
                    : BiTerminalGraph.sequenceOf(
                    		new NativeExprGraphFactory(location.getIndex(), scope, inMethodCall).getGraph(),
                    		BiTerminalGraph.ofInstructions(
                                    pop(Register.R11),
                                    move(Register.RBP, Register.R10)));

        } else if (scopeType == ScopeType.GLOBAL) {
            return !check
            		? BiTerminalGraph.sequenceOf(
            		// The size of the array for bounds checking, we will trash this in the checker
            		BiTerminalGraph.ofInstructions(
            				push(new Literal(scope.getFromScope(location.getVariable()).get().getLength().get().get64BitValue()))
            				),
                    new NativeExprGraphFactory(location.getIndex(), scope, inMethodCall).getGraph(),
                    // We are going to borrow the array index and
                    // Check it boundaries against the array size
                    new ArrayBoundsCheckGraphFactory().getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(Register.R11),
                            movePointer(new Label(LabelType.GLOBAL, location.getVariable()),
                                    Register.R10)))
                    : BiTerminalGraph.sequenceOf(
                    		new NativeExprGraphFactory(location.getIndex(), scope, inMethodCall).getGraph(),
                    		BiTerminalGraph.ofInstructions(
                                    pop(Register.R11),
                                    move(Register.RBP, Register.R10)));
        } else {
            throw new AssertionError("Unexepected ScopeType for array: " + scopeType);
        }
    }
    
    private static long offset(ArrayLocation location, Scope scope) {
        return scope.offsetFromBasePointer(location.getVariable())
                * Architecture.WORD_SIZE
                * -1;
    }

    private BiTerminalGraph calculateLoadFromScalar(ScalarLocation location, Scope scope, boolean inMethodCall) {
        Variable var = location.getVariable();
        return BiTerminalGraph.ofInstructions(
                move(new VariableReference(var, scope, inMethodCall), R11),
                push(R11));
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
