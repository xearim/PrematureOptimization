package edu.mit.compilers.common.variableordering;

import static edu.mit.compilers.ast.BinaryOperator.MINUS;
import static edu.mit.compilers.ast.BinaryOperator.PLUS;
import static edu.mit.compilers.ast.BinaryOperator.TIMES;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.ast.UnaryOperator;

public abstract class ExpressionOrdering {
	
	protected ExpressionOrdering(){};
	
	private static Set<BinaryOperator> COMMUNICATIVE_OPS =
			ImmutableSet.of(PLUS, TIMES);
	
	public NativeExpression order(NativeExpression expr){
		return buildOrderedExpression(expr);
	}
	
	public Set<NativeExpression> order(Set<NativeExpression> expressions){
		HashSet<NativeExpression> output = new HashSet<NativeExpression>();
		for(NativeExpression expr : expressions){
			output.add(buildOrderedExpression(expr));
		}
		return output;
	}
	
	private NativeExpression buildOrderedExpression(NativeExpression expr){
		switch(expr.getType()){
		case ARRAY_LOCATION:  // Fall - Through
		case SCALAR_LOCATION:  // Fall - Through
		case BOOLEAN_LITERAL:  // Fall - Through
		case CHAR_LITERAL:  // Fall - Through
		case INT_LITERAL:
			// Do not need to modify singletons
			return expr;
		case UNARY_OPERATION:
			return buildOrderedExpression((UnaryOperation) expr);
		case BINARY_OPERATION:
			return buildOrderedExpression((BinaryOperation) expr);
		case METHOD_CALL:
			return buildOrderedExpression((MethodCall) expr);
		case TERNARY_OPERATION:
			TernaryOperation ternary = (TernaryOperation) expr;
			return new TernaryOperation(
					buildOrderedExpression(ternary.getCondition()),
					buildOrderedExpression(ternary.getTrueResult()),
					buildOrderedExpression(ternary.getFalseResult()));
		default:
			throw new AssertionError("Recieved NativeExpression of invalid type" + expr.getType());
		}
	}
	private UnaryOperation buildOrderedExpression(UnaryOperation expr){
		return new UnaryOperation(expr.getOperator(), buildOrderedExpression(expr.getArgument()));
	}
	
	private BinaryOperation buildOrderedExpression(BinaryOperation expr){
		NativeExpression leftArg = expr.getLeftArgument();
		NativeExpression rightArg = expr.getRightArgument();
		
		switch(expr.getOperator()){
		case AND: // Cant change ordering due to short-circuit requirement
		case OR: // Fall-through
			return new BinaryOperation(
					expr.getOperator(),
					buildOrderedExpression(leftArg),
					buildOrderedExpression(rightArg)
					);
		case DIVIDED_BY: // Non-communicative math operators
		case MODULO: // Fall-through
		case MINUS:
			return new BinaryOperation(
					expr.getOperator(),
					buildOrderedExpression(leftArg),
					buildOrderedExpression(rightArg)
					);
		case GREATER_THAN: // Non-communicative relational operators
		case LESS_THAN: // Fall-through
		case GREATER_THAN_OR_EQUAL: // Fall-through
		case LESS_THAN_OR_EQUAL: // Fall-through
			return new BinaryOperation(
					expr.getOperator(),
					buildOrderedExpression(leftArg),
					buildOrderedExpression(rightArg)
					);
		case DOUBLE_EQUALS: // Non-communicative but re-orderable operators
		case NOT_EQUALS: // Fall-through
			return (BinaryOperation) orderExpressions(leftArg, rightArg, expr.getOperator()); 
		case PLUS: // Normal Communicative operators, therefore reorderable with themselves
		case TIMES:
			return (BinaryOperation) orderExpressions(getMoveableExpressions(expr, expr.getOperator()), expr.getOperator()); 
		default:
			throw new AssertionError("Somehow got invalid binary expression with operator " + expr.getOperator().getSymbol());
		}
	}
	
	private Set<NativeExpression> getMoveableExpressions(NativeExpression expr, BinaryOperator op){
		HashSet<NativeExpression> moveableExpressions = new HashSet<NativeExpression>();		
		if((expr instanceof BinaryOperation) &&
		   communicativeOperators(((BinaryOperation) expr).getOperator(), op)){
			BinaryOperation binOp = (BinaryOperation) expr;
			moveableExpressions.addAll(getMoveableExpressions(binOp.getLeftArgument(), binOp.getOperator()));
			moveableExpressions.addAll(getMoveableExpressions(binOp.getRightArgument(), binOp.getOperator()));
		} else {
			moveableExpressions.add(buildOrderedExpression(expr));
		}
		return moveableExpressions;
	}
	
	private boolean communicativeOperators(BinaryOperator x, BinaryOperator y){
		return (x == y && COMMUNICATIVE_OPS.contains(x));
	}
	
	// Dangerous, but all expression orderings are going to have to implement this or they will break
	protected NativeExpression orderExpressions(Set<NativeExpression> expressions, BinaryOperator op){
		throw new AssertionError("Custom expression ordering does not define an expression ordering");
	}
	
	protected NativeExpression orderExpressions(
			NativeExpression leftExpr, NativeExpression rightExpr, BinaryOperator op){
		throw new AssertionError("Custom expression ordering does not define an expression ordering");
	}
	
	
	private MethodCall buildOrderedExpression(MethodCall expr){
		ImmutableList.Builder<GeneralExpression> builder = ImmutableList.builder();
		for(GeneralExpression methodExpr : expr.getParameterValues().getChildren()){
			if(methodExpr instanceof NativeExpression){
				builder.add(buildOrderedExpression((NativeExpression) methodExpr));
			} else {
				builder.add(methodExpr);
			}
		}
		return new MethodCall(expr.getMethodName(), builder.build());
	}

}
