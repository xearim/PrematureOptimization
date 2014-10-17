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
import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compare;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;

public class BinOpGraphFactory implements GraphFactory {

    private static final Set<BinaryOperator> ARITHMETIC_OPS =
            ImmutableSet.of(PLUS, MINUS, TIMES, DIVIDED_BY, MODULO);
    private static final Set<BinaryOperator> LOGIC_OPS = ImmutableSet.of(AND, OR);
    private static final Set<BinaryOperator> COMPARISON_OPS =
            ImmutableSet.of(DOUBLE_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL,
                    LESS_THAN, LESS_THAN_OR_EQUAL, NOT_EQUALS);

    private final BinaryOperation binOp;
    private final Scope scope;
    private final BiTerminalGraph graph;
    
    public BinOpGraphFactory(BinaryOperation binOp, Scope scope) {
        this.binOp = binOp;
        this.scope = scope;
        this.graph = calculateOperation();
    }
    
    private BiTerminalGraph calculateOperation() {
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
 
    private BiTerminalGraph calculateArithmeticOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(ARITHMETIC_OPS.contains(operator));

        return BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
                new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(R10),
                        pop(R11),
                        arithmeticOperator(binOp.getOperator(), R10, R11),
                        push(R11)
                        ));
    }

    private Instruction arithmeticOperator(BinaryOperator operator, Register operand, Register target) {
        switch (operator) {
            case DIVIDED_BY:
                return Instructions.divide(operand, target);
            case MINUS:
                return Instructions.subtract(operand, target);
            case MODULO:
                return Instructions.modulo(operand, target);
            case PLUS:
                return Instructions.add(operand, target);
            case TIMES:
                return Instructions.multiply(operand, target);
            default:
                throw new AssertionError("Unexpected arithmetic operator: " + operator);
        }
    }

    private BiTerminalGraph calculateLogicOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(LOGIC_OPS.contains(operator));

        return BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
                new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(R10),
                        pop(R11),
                        logicalOperator(binOp.getOperator(), R10, R11),
                        push(R11)
                        ));
    }
    
    private Instruction logicalOperator(BinaryOperator operator, Register operand, Register target) {
        switch (operator) {
            case AND:
                return Instructions.and(operand, target);
            case OR:
                return Instructions.or(operand, target);
            default:
                throw new AssertionError("Unexpected logical operator: " + operator);
        }
    }

    private BiTerminalGraph calculateComparisonOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(COMPARISON_OPS.contains(operator));


        return BiTerminalGraph.sequenceOf(
	        new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
	        new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph(),
	        BiTerminalGraph.ofInstructions(
	                pop(R10),
	                pop(R11),
	                compare(binOp.getOperator(), R10, R11),
	                push(R11)
	                ));
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
