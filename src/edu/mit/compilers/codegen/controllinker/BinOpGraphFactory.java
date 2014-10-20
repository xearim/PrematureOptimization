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
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.asm.instructions.JumpType;

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
        
        switch(binOp.getOperator()) {
        	case AND:
        		return calculateShortCircutAnd();
        	case OR:
        		return calculateShortCircutOr();
        	default:
        		throw new AssertionError("Unexpected logical operator: " + operator);
        }
    }
    
    private BiTerminalGraph calculateShortCircutAnd() {
    	// Our short circuiting return value (for and we short circuit to false)
    	SequentialControlFlowNode ReturnFalse = SequentialControlFlowNode.terminal(push(Literal.FALSE));
    	
    	// The left hand expression to be evaluated and branched off of
    	BiTerminalGraph LeftHandExpression = BiTerminalGraph.sequenceOf(
    						new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
    						BiTerminalGraph.ofInstructions(
    								pop(R10),
    								move(Literal.TRUE, R11),
    								compareFlagged(R10, R11)));
    	
    	// The right hand expression, we will use its value as the return value if we reach it
    	BiTerminalGraph RightHandExpression = BiTerminalGraph.sequenceOf(
				new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph());
    	
    	// The short circuit branch, if we are true for the right hand, we need to 
    	// evaluate the left, if not, we can just straight return false
    	BranchingControlFlowNode ShortCircuit = new BranchingControlFlowNode(
    													JumpType.JE,
    													ReturnFalse,
    													RightHandExpression.getBeginning());
    					
    	// Where we join the control flow back up after an AND
    	SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
    	
    	// Link it up
    	LeftHandExpression.getEnd().setNext(ShortCircuit);
    	RightHandExpression.getEnd().setNext(end);
    	ReturnFalse.setNext(end);
    	
    	return new BiTerminalGraph(LeftHandExpression.getBeginning(), end);
    }
    
    private BiTerminalGraph calculateShortCircutOr() {
    	// Our short circuiting return value (for or we short circuit to true)
    	SequentialControlFlowNode ReturnTrue = SequentialControlFlowNode.terminal(push(Literal.TRUE));
    	
    	// The left hand expression to be evaluated and branched off of
    	BiTerminalGraph LeftHandExpression = BiTerminalGraph.sequenceOf(
    						new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
    						BiTerminalGraph.ofInstructions(
    								pop(R10),
    								move(Literal.TRUE, R11),
    								compareFlagged(R10, R11)));
    	
    	// The right hand expression, we will use its value as the return value if we reach it
    	BiTerminalGraph RightHandExpression = BiTerminalGraph.sequenceOf(
				new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph());
    	
    	// The short circuit branch, if we are false for the right hand, we need to 
    	// evaluate the left, if not, we can just straight return true
    	BranchingControlFlowNode ShortCircuit = new BranchingControlFlowNode(
    													JumpType.JE,
    													RightHandExpression.getBeginning(),
    													ReturnTrue);
    					
    	// Where we join the control flow back up after an OR
    	SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
    	
    	// Link it up
    	LeftHandExpression.getEnd().setNext(ShortCircuit);
    	RightHandExpression.getEnd().setNext(end);
    	ReturnTrue.setNext(end);
    	
    	return new BiTerminalGraph(LeftHandExpression.getBeginning(), end);
    }

    private BiTerminalGraph calculateComparisonOperation() {
        BinaryOperator operator = binOp.getOperator();
        checkState(COMPARISON_OPS.contains(operator));


        return BiTerminalGraph.sequenceOf(
	        new NativeExprGraphFactory(binOp.getLeftArgument(), scope).getGraph(),
	        new NativeExprGraphFactory(binOp.getRightArgument(), scope).getGraph(),
	        BiTerminalGraph.ofInstructions(
			pop(R11), // Right argument.
			pop(R10), // Left argument.
	                compare(binOp.getOperator(), R10, R11),
	                push(R11)
	                ));
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
