package edu.mit.compilers.codegen.controllinker;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.compilers.ast.BinaryOperator.AND;
import static edu.mit.compilers.ast.BinaryOperator.DIVIDED_BY;
import static edu.mit.compilers.ast.BinaryOperator.MINUS;
import static edu.mit.compilers.ast.BinaryOperator.MODULO;
import static edu.mit.compilers.ast.BinaryOperator.OR;
import static edu.mit.compilers.ast.BinaryOperator.PLUS;
import static edu.mit.compilers.ast.BinaryOperator.*;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.codegen.ControlFlowNode;

public class BinOpLinker implements ControlLinker {

    private final BinaryOperation binOp;

    public BinOpLinker(BinaryOperation binOp) {
        this.binOp = binOp;
    }

    @Override
    public ControlFlowNode linkTo(ControlFlowNode sink) {
        switch(binOp.getOperator()) {
            case PLUS:
            case MINUS:
            case TIMES:
            case DIVIDED_BY:
            case MODULO:
                return arithmeticLinker(sink);
            case AND:
            case OR:
                return logicLinker(sink);
            case DOUBLE_EQUALS:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case NOT_EQUALS:
                return comparisonLinker(sink);
            default:
                throw new AssertionError("Unexpected operator: " + binOp.getOperator());
        }
    }

    private ControlFlowNode arithmeticLinker(ControlFlowNode sink) {
        BinaryOperator operator = binOp.getOperator();
        checkState(ImmutableList.of(PLUS, MINUS, TIMES, DIVIDED_BY, MODULO)
                .contains(operator));

        return new SequentialControlLinker()
        .append(new NativeExprLinker(binOp.getLeftArgument()))
        .append(new NativeExprLinker(binOp.getRightArgument()))
        .append(InstructionControlLinker.of(
                // TODO(jasonpr): Make this work!
                //pop(R10),
                //pop(R11),
                //mathOp(binOp.getOperator(), R10, R11),
                //push(R11)
                ))
        .linkTo(sink);
    }

    private ControlFlowNode logicLinker(ControlFlowNode sink) {
        BinaryOperator operator = binOp.getOperator();
        checkState(ImmutableList.of(AND, OR).contains(operator));

        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }

    private ControlFlowNode comparisonLinker(ControlFlowNode sink) {
        BinaryOperator operator = binOp.getOperator();
        checkState(ImmutableList.of(DOUBLE_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL,
                LESS_THAN, LESS_THAN_OR_EQUAL, NOT_EQUALS).contains(operator));

        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
}
