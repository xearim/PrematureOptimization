package edu.mit.compilers.codegen.controllinker;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.compilers.ast.BinaryOperator.AND;
import static edu.mit.compilers.ast.BinaryOperator.DIVIDED_BY;
import static edu.mit.compilers.ast.BinaryOperator.DOUBLE_EQUALS;
import static edu.mit.compilers.ast.BinaryOperator.GREATER_THAN;
import static edu.mit.compilers.ast.BinaryOperator.GREATER_THAN_OR_EQUAL;
import static edu.mit.compilers.ast.BinaryOperator.LESS_THAN;
import static edu.mit.compilers.ast.BinaryOperator.LESS_THAN_OR_EQUAL;
import static edu.mit.compilers.ast.BinaryOperator.MINUS;
import static edu.mit.compilers.ast.BinaryOperator.MODULO;
import static edu.mit.compilers.ast.BinaryOperator.NOT_EQUALS;
import static edu.mit.compilers.ast.BinaryOperator.OR;
import static edu.mit.compilers.ast.BinaryOperator.PLUS;
import static edu.mit.compilers.ast.BinaryOperator.TIMES;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;

public class BinOpGraphFactory implements GraphFactory {

    private static final Set<BinaryOperator> ARITHMETIC_OPS =
            ImmutableSet.of(PLUS, MINUS, TIMES, DIVIDED_BY, MODULO);
    private static final Set<BinaryOperator> LOGIC_OPS = ImmutableSet.of(AND, OR);
    private static final Set<BinaryOperator> COMPARISON_OPS =
            ImmutableSet.of(DOUBLE_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL,
                    LESS_THAN, LESS_THAN_OR_EQUAL, NOT_EQUALS);

    private final BinaryOperation binOp;

    private final TerminaledGraph graph;
    
    public BinOpGraphFactory(BinaryOperation binOp) {
        this.binOp = binOp;
        this.graph = calculateOperation();
    }
    
    private TerminaledGraph calculateOperation() {
        switch(binOp.getOperator()) {
            // TODO(manny): Factor this enum out from ast package into a compilers.common package.
            case PLUS:
            case MINUS:
            case TIMES:
            case DIVIDED_BY:
            case MODULO:
                return calculateArithmeticOperation();
            case AND:
            case OR:
                return calculateLogicOperation();
            case DOUBLE_EQUALS:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case NOT_EQUALS:
                return calculateComparisonOperation();
            default:
                throw new AssertionError("Unexpected operator: " + binOp.getOperator());
        }
    }
 
    private TerminaledGraph calculateArithmeticOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(ARITHMETIC_OPS.contains(operator));

        return TerminaledGraph.sequenceOf(
                new NativeExprGraphFactory(binOp.getLeftArgument()).getGraph(),
                new NativeExprGraphFactory(binOp.getRightArgument()).getGraph(),
                TerminaledGraph.ofInstructions(
                        // TODO(manny): Make this work!
                        //pop(R10),
                        //pop(R11),
                        //mathOp(binOp.getOperator(), R10, R11),
                        //push(R11)
                        ));
    }

    private TerminaledGraph calculateLogicOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(LOGIC_OPS.contains(operator));

        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }

    private TerminaledGraph calculateComparisonOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(COMPARISON_OPS.contains(operator));

        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }
}
