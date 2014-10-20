package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveFromMemory;
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
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.VariableReference;


public class VariableLoadGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;

    public VariableLoadGraphFactory(Location location, Scope scope) {
        this.graph = calculateLoad(location, scope);
    }

    private BiTerminalGraph calculateLoad(Location location, Scope scope) {
        if (location instanceof ArrayLocation) {
            return calculateLoadFromArray((ArrayLocation) location, scope);
        } else if (location instanceof ScalarLocation) {
            return calculateLoadFromScalar((ScalarLocation) location, scope);
        } else {
            throw new AssertionError("Unexpected location type for " + location);
        }
    }

    private static BiTerminalGraph calculateLoadFromArray(ArrayLocation location, Scope scope) {
        return BiTerminalGraph.sequenceOf(
                setupArrayRegisters(location, scope),
                BiTerminalGraph.ofInstructions(
                        moveFromMemory(
                                Register.R11, Register.R10, Register.RAX,
                                new Literal(Architecture.WORD_SIZE), Register.R10),
                        add(new Literal(3*Architecture.WORD_SIZE), Register.RSP),
                        push(Register.R10)));
    }
    // TODO(jasonpr): Have this code live somewhere sensible.
    public static BiTerminalGraph calculateStoreToArray(ArrayLocation location, Scope scope) {
        return BiTerminalGraph.sequenceOf(
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10), // Pop value to store into R10.
                        push(Register.R9), // Push R9, so we can use it as temp.
                        move(Register.R10, Register.R9)), // Free up R10 for later use.
                setupArrayRegisters(location, scope),
                BiTerminalGraph.ofInstructions(
                        moveToMemory(Register.R9, Register.R11, Register.R10, Register.RAX,
                                new Literal(Architecture.WORD_SIZE)),
                        add(new Literal(3*Architecture.WORD_SIZE), Register.RSP),
                        pop(Register.R9))); // Restore R9.
    }
    
    /**
     * Load values into R10, R11, and RAX so that '%r11(%r10, %rax, 8)' refers to the array
     * location.
     */
    public static BiTerminalGraph setupArrayRegisters(ArrayLocation location, Scope scope) {
        Scope immediateScope = scope.getLocation(location.getName());
        ScopeType scopeType = immediateScope.getScopeType();
        if (scopeType == ScopeType.LOCAL) {
            return BiTerminalGraph.sequenceOf(
                    // Locals are offset from stack pointer.
                    new NativeExprGraphFactory(location.getIndex(), scope).getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(Register.RAX),
                            move(Register.RBP, Register.R10),
                            move(new Literal(scope.offsetFromBasePointer(location.getName())),
                                    Register.R11)));
        } else if (scopeType == ScopeType.GLOBAL) {
            return BiTerminalGraph.sequenceOf(
                    new NativeExprGraphFactory(location.getIndex(), scope).getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(Register.RAX),
                            move(new Label(LabelType.GLOBAL, location.getName()),
                                    Register.R10),
                            move(new Literal(0), Register.R11)));
        } else {
            throw new AssertionError("Unexepected ScopeType for array: " + scopeType);
        }
    }

    private BiTerminalGraph calculateLoadFromScalar(ScalarLocation location, Scope scope) {
        String name = location.getName();
        return BiTerminalGraph.ofInstructions(
                move(new VariableReference(name, scope), R11),
                push(R11));
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
