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
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compare;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class BinOpGraphFactory implements GraphFactory {

    private static final Set<BinaryOperator> ARITHMETIC_OPS =
            ImmutableSet.of(PLUS, MINUS, TIMES, DIVIDED_BY, MODULO);
    private static final Set<BinaryOperator> LOGIC_OPS = ImmutableSet.of(AND, OR);
    private static final Set<BinaryOperator> COMPARISON_OPS =
            ImmutableSet.of(DOUBLE_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL,
                    LESS_THAN, LESS_THAN_OR_EQUAL, NOT_EQUALS);

    private final BinaryOperation binOp;
    private final Scope scope;

    public BinOpGraphFactory(BinaryOperation binOp, Scope scope) {
        this.binOp = binOp;
        this.scope = scope;
    }

    private FlowGraph<Instruction> calculateOperation() {
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

    private FlowGraph<Instruction> calculateArithmeticOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(ARITHMETIC_OPS.contains(operator));

        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph())
                .append(new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph())
                .append(pop(R10))
                .append(pop(R11))
                .append(arithmeticOperator(binOp.getOperator(), R10, R11))
                .append(push(R11))
                .build();
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

    private FlowGraph<Instruction> calculateLogicOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(LOGIC_OPS.contains(operator));
        
        switch(binOp.getOperator()) {
        	case AND:
        		return calculateShortCircutAnd();
        	case OR:
        		return calculateShortCircutOr();
        	default:
        		throw new AssertionError("Unexpected logical operator: " + operator);
        }
    }

    private FlowGraph<Instruction> calculateShortCircutAnd() {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();

        builder.append(new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph())
                .append(pop(R10))
                .append(compareFlagged(R10, Literal.TRUE));

        Node<Instruction> shortCircuitBranch = Node.nop();
        FlowGraph<Instruction> rightHandExpression =
                new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph();

        Node<Instruction> shortCircuitPushFalse = Node.of(push(Literal.FALSE));
        builder.append(shortCircuitBranch)
                .linkNonJumpBranch(shortCircuitBranch, rightHandExpression)
                .linkJumpBranch(shortCircuitBranch, JumpType.JNE, shortCircuitPushFalse)
                .setEndToSinkFor(rightHandExpression.getEnd(), shortCircuitPushFalse);
        return builder.build();
    }

    // TODO(jasonpr): Factor out common code from here and calculateShortCircuitAnd().
    private FlowGraph<Instruction> calculateShortCircutOr() {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();

        builder.append(new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph())
                .append(pop(R10))
                .append(compareFlagged(R10, Literal.FALSE));

        Node<Instruction> shortCircuitBranch = Node.nop();
        FlowGraph<Instruction> rightHandExpression =
                new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph();

        Node<Instruction> shortCircuitPushTrue = Node.of(push(Literal.TRUE));
        builder.append(shortCircuitBranch)
                .linkNonJumpBranch(shortCircuitBranch, rightHandExpression)
                .linkJumpBranch(shortCircuitBranch, JumpType.JNE, shortCircuitPushTrue)
                .setEndToSinkFor(rightHandExpression.getEnd(), shortCircuitPushTrue);

        return builder.build();
    }

    private FlowGraph<Instruction> calculateComparisonOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(COMPARISON_OPS.contains(operator));

        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph())
                .append(new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph())
                .append(pop(R11)) // Right argument.
                .append(pop(R10)) // Left argument.
	            .append(compare(binOp.getOperator(), R10, R11))
	            .append(push(R11))
	            .build();
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return calculateOperation();
    }
}
