package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveFromMemory;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.movePointer;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveToMemory;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

import com.sun.corba.se.impl.logging.ActivationSystemException;

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
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;


public class VariableLoadGraphFactory implements GraphFactory {

    private final Location location;
    private final Scope scope;

    public VariableLoadGraphFactory(Location location, Scope scope) {
        this.location = location;
        this.scope = scope;
    }

    private static FlowGraph<Instruction> calculateLoadFromArray(ArrayLocation location, Scope scope, boolean check) {
        return BasicFlowGraph.<Instruction>builder()
                .append(setupArrayRegisters(location, scope, check))
                .append(moveFromMemory(offset(location, scope), R10, R11,
                        Architecture.WORD_SIZE, R10))
                .append(push(R10)).build();
    }

    // TODO(jasonpr): Have this code live somewhere sensible.
    public static FlowGraph<Instruction> calculateStoreToArray(ArrayLocation location, Scope scope, boolean check) {
        return BasicFlowGraph.<Instruction>builder()
                .append(setupArrayRegisters(location, scope, check))
                // Pop value to store after, so we can't corrupt it.
                .append(pop(Register.RAX))
                .append(moveToMemory(Register.RAX, offset(location, scope), Register.R10,
                        Register.R11, Architecture.WORD_SIZE))
                .build();
    }

    /**
     * Load values into R10, and R11 so that 'X(%r10, %r11, 8)' refers to the array
     * location.
     */
    public static FlowGraph<Instruction> setupArrayRegisters(ArrayLocation location, Scope scope, boolean check) {
        Scope immediateScope = scope.getLocation(location.getVariable());
        ScopeType scopeType = immediateScope.getScopeType();
        // Loads the "base" of the array into R10.  For a global, that's the actual location of the
        // array. For a local, it's just RBP, and we can find the array by offsetting from this
        // base.
        Instruction arrayBaseLoader;
        if (scopeType == ScopeType.LOCAL) {
            arrayBaseLoader = move(Register.RBP, Register.R10);
        } else if (scopeType == ScopeType.GLOBAL) {
            arrayBaseLoader = movePointer(
                    new Label(LabelType.GLOBAL, location.getVariable()), Register.R10);
        } else {
            throw new AssertionError("Unexpected ScopeType for array: " + scopeType);
        }

        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();
        builder.append(new NativeExprGraphFactory(location.getIndex(), scope).getGraph());
        if (check) {
            builder.append(new ArrayBoundsCheckGraphFactory(
                    scope.getFromScope(location.getVariable()).get().getLength().get()).getGraph());
        }
        builder.append(pop(Register.R11)).append(arrayBaseLoader);
        return builder.build();
    }
    
    private static long offset(ArrayLocation location, Scope scope) {
        return scope.offsetFromBasePointer(location.getVariable())
                * Architecture.WORD_SIZE
                * -1;
    }

    private static FlowGraph<Instruction> calculateLoadFromScalar(ScalarLocation location, Scope scope) {
        return BasicFlowGraph.<Instruction>builder()
                .append(move(new VariableReference(location.getVariable(), scope), R11))
                .append(push(R11))
                .build();
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        if (location instanceof ArrayLocation) {
            return calculateLoadFromArray((ArrayLocation) location, scope, true);
        } else if (location instanceof ScalarLocation) {
            return calculateLoadFromScalar((ScalarLocation) location, scope);
        } else {
            throw new AssertionError("Unexpected location type for " + location);
        }
    }
}
