package edu.mit.compilers.optimization.cse;

import java.util.Set;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.NativeExpression;

public class LeftAssociative extends ExpressionOrdering{
	
	public LeftAssociative(){
		super();
	}
	
	@SuppressWarnings("unused")
	private NativeExpression orderExpressions(Set<NativeExpression> expressions, BinaryOperator op){
		NativeExpression head = null;
		for(NativeExpression expr : expressions){
			if(head == null){
				head = expr;
			} else {
				head = head.compareTo(expr) >= 0 ? head : expr;
			}
		}
		expressions.remove(head);
		return expressions.isEmpty()
			 ? head
			 : new BinaryOperation(op, orderExpressions(expressions, op), head);
	}
	
	@SuppressWarnings("unused")
	private NativeExpression orderExpressions(
			NativeExpression leftExpr, NativeExpression rightExpr, BinaryOperator op){
		return leftExpr.compareTo(rightExpr) >= 0
			 ? new BinaryOperation(op, leftExpr, rightExpr)
			 : new BinaryOperation(op, rightExpr, leftExpr);
	}

}
