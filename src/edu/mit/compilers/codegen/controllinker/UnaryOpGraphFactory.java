package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.negate;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.not;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

import java.util.Map;

import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;


public class UnaryOpGraphFactory implements GraphFactory {

    private final UnaryOperation operation;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public UnaryOpGraphFactory(UnaryOperation operation, Scope scope, Map<ScopedVariable, Register> allocations) {
        this.operation = operation;
        this.scope = scope;
        this.allocations = allocations;
    }

    private FlowGraph<Instruction> calculateLengthOperation() {
        // TODO(jasonpr): Eliminate the need for this cast.
        Location targetLocation = (Location) operation.getArgument();
        // If these get()s fail, our semantic checker is broken.
        FieldDescriptor descriptor = scope.getFromScope(targetLocation.getVariable()).get();
        IntLiteral length = descriptor.getLength().get();
        return BasicFlowGraph.<Instruction>builder()
                .append(push(new Literal(length)))
                .build();
    }

    private FlowGraph<Instruction> calculateNegativeOperation() {
        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(operation.getArgument(), scope, allocations).getGraph())
                .append(pop(R10))
                .append(negate(R10))
                .append(push(R10))
                .build();
    }

    private FlowGraph<Instruction> calculateNotOperation() {
        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(operation.getArgument(), scope, allocations).getGraph())
                .append(pop(R10))
                .append(not(R10))
                .append(push(R10))
                .build();
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        switch (operation.getOperator()) {
            case ARRAY_LENGTH:
                return calculateLengthOperation();
            case NEGATIVE:
                return calculateNegativeOperation();
            case NOT:
                return calculateNotOperation();
            default:
                throw new AssertionError("Unexpected unary operator " + operation.getOperator());
        }
    }

}
