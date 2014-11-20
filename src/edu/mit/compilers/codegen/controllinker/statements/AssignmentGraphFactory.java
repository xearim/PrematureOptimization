package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.subtract;
import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.codegen.controllinker.VariableLoadGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

public class AssignmentGraphFactory implements GraphFactory {

    private final NativeExpression expr;
    private final AssignmentOperation op;
    private final Scope scope;
    private final Location target;
    private final boolean check;

    public AssignmentGraphFactory(Location target, AssignmentOperation op, NativeExpression expr, Scope scope, boolean check){
        this.target = target;
        this.op = op;
        this.expr = expr;
        this.scope = scope;
        this.check = check;
    }

    public AssignmentGraphFactory(Assignment assignment, Scope scope) {
        this(assignment.getLocation(), assignment.getOperation(), assignment.getExpression(),
                scope, true /* Do check array bounds. */);
    }

    private FlowGraph<Instruction> calculateStore(Location target, Scope scope){
        if (target instanceof ArrayLocation) {
            return calculateStoreToArray((ArrayLocation) target, scope);
        } else if (target instanceof ScalarLocation) {
            return calculateStoreToScalar((ScalarLocation) target, scope);
        } else {
            throw new AssertionError("Unexpected location type for " + target);
        }
    }

    private FlowGraph<Instruction> calculateStoreToArray(ArrayLocation target, Scope scope){
        // TODO(jasonpr): Have this code live somewhere sensible.
        return VariableLoadGraphFactory.calculateStoreToArray(target, scope, check);
    }

    private FlowGraph<Instruction> calculateStoreToScalar(ScalarLocation target, Scope scope){
        return BasicFlowGraph.<Instruction>builder()
                .append(pop(R11))
                .append(move(R11, new VariableReference(target.getVariable(), scope)))
                .build();
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();
        switch(op){
        case MINUS_EQUALS:
            return builder.append(new VariableLoadGraphFactory(target, scope).getGraph())
                    .append(new NativeExprGraphFactory(expr, scope).getGraph())
                    .append(pop(R10))
                    .append(pop(R11))
                    .append(subtract(R10, R11))
                    .append(push(R11))
                    .append(calculateStore(target, scope))
                    .build();
        case PLUS_EQUALS:
            return builder.append(new VariableLoadGraphFactory(target, scope).getGraph())
                    .append(new NativeExprGraphFactory(expr, scope).getGraph())
                    .append(pop(R10))
                    .append(pop(R11))
                    .append(add(R10, R11))
                    .append(push(R11))
                    .append(calculateStore(target, scope))
                    .build();
        case SET_EQUALS:
            return builder.append(new NativeExprGraphFactory(expr, scope).getGraph())
                    .append(calculateStore(target, scope))
                    .build();
        default:
            throw new AssertionError("Unexpected operator: " + op.getSymbol());
        }
    }
}
