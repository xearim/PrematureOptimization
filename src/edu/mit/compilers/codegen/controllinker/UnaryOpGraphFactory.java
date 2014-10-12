package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.increment;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.negate;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.asm.Literal;


public class UnaryOpGraphFactory implements GraphFactory {

    private final UnaryOperation operation;
    private final TerminaledGraph graph;

    public UnaryOpGraphFactory(UnaryOperation operation, Scope scope) {
        this.operation = operation;
        this.graph = calculateOperation(scope);
    }

    private TerminaledGraph calculateOperation(Scope scope) {
        switch (operation.getOperator()) {
            case ARRAY_LENGTH:
                return calculateLengthOperation(scope);
            case NEGATIVE:
                return calculateNegativeOperation(scope);
            case NOT:
                return calculateNotOperation(scope);
            default:
                throw new AssertionError("Unexpected unary operator " + operation.getOperator());
        }
    }

    private TerminaledGraph calculateLengthOperation(Scope scope) {
        // TODO(jasonpr): Eliminate the need for this cast.
        Location targetLocation = (Location) operation.getArgument();
        // If these get()s fails, our semantic checker is broken.
        FieldDescriptor descriptor = scope.getFromScope(targetLocation.getVariableName()).get();
        IntLiteral length = descriptor.getLength().get();
        return TerminaledGraph.ofInstructions(push(new Literal(length)));
    }

    private TerminaledGraph calculateNegativeOperation(Scope scope) {
        return TerminaledGraph.sequenceOf(
                new NativeExprGraphFactory(operation.getArgument(), scope).getGraph(),
                TerminaledGraph.ofInstructions(
                        pop(R10),
                        negate(R10),
                        push(R10)));
    }

    private TerminaledGraph calculateNotOperation(Scope scope) {
        // We represent true as 0x1 and false as 0x0. So, boolean NOT is just
        // x = -x + 1.
        return TerminaledGraph.sequenceOf(
                new NativeExprGraphFactory(operation.getArgument(), scope).getGraph(),
                TerminaledGraph.ofInstructions(
                        pop(R10),
                        negate(R10),
                        increment(R10),
                        push(R10)));
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }

}
