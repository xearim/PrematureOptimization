package edu.mit.compilers.ir;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.*;

public class EvaluateCheck {
	/**
	 * A purely static class that allows one to check a NativeExpression for
	 * its plausible return type given a scope, an absent type means the expression
	 * does not actually evaluate to anything and should prompt a semantic error
	 */
	
	private EvaluateCheck(){};
	
	public static Optional<BaseType> evaluatesTo(NativeExpression expression, Scope scope){
		return Optional.absent();
	}
	
	public static Optional<BaseType> evaluatesTo(BinaryOperation expression, Scope scope){
		switch(expression.getOperator()){
		case AND: /* fall-through */
		case OR: /* fall-through */
		case DOUBLE_EQUALS: /* fall-through */
		case NOT_EQUALS: /* fall-through */
		case GREATER_THAN_OR_EQUAL: /* fall-through */
		case GREATER_THAN: /* fall-through */
		case LESS_THAN_OR_EQUAL: /* fall-through */
		case LESS_THAN: /* fall-through */
			return Optional.of(BaseType.BOOLEAN);
		case DIVIDED_BY: /* fall-through */
		case MINUS: /* fall-through */
		case MODULO: /* fall-through */
		case PLUS: /* fall-through */
		case TIMES: /* fall-through */
			return Optional.of(BaseType.INTEGER);
		// Should never reach here
		default:
			throw new AssertionError("Binary Operations are not valid for operator " + expression.getOperator());
		}
	}
	
	public static Optional<BaseType> evaluatesTo(UnaryOperation expression, Scope scope){
		switch(expression.getOperator()){
		case ARRAY_LENGTH:
			return Optional.of(BaseType.INTEGER);
		case NEGATIVE:
			return Optional.of(BaseType.INTEGER);
		case NOT:
			return Optional.of(BaseType.BOOLEAN);
		default:
			throw new AssertionError("Binary Operations are not valid for operator " + expression.getOperator());
		}
	}
	
	// Ternaray Ops need to be checked on both children and equating them
	// So just recurse, attempting to return the eval of a ternary would require
	// A lot of method exposure or a dirty hack
	public static Optional<BaseType> evaluatesTo(TernaryOperation expression, Scope scope){
		return Optional.absent();
	}
	
	public static Optional<BaseType> evaluatesTo(BooleanLiteral expression, Scope scope){
		return Optional.of(BaseType.BOOLEAN);
	}
	
	public static Optional<BaseType> evaluatesTo(CharLiteral expression, Scope scope){
		return Optional.of(BaseType.VOID);
	}
	
	public static Optional<BaseType> evaluatesTo(IntLiteral expression, Scope scope){
		return Optional.of(BaseType.INTEGER);
	}
	
	public static Optional<BaseType> evaluatesTo(StringLiteral expression, Scope scope){
		return Optional.of(BaseType.VOID);
	}
	
	public static Optional<BaseType> evaluatesTo(MethodCall expression, Iterable<Method> methodTable){
		for(Method method : methodTable){
			if(expression.getName().equals(method.getName()))
				return method.getReturnType().getReturnType();
		}
		return Optional.absent();
	}
	
	public static Optional<BaseType> evaluatesTo(ScalarLocation expression, Scope scope){
		Optional<FieldDescriptor> var = scope.getFromScope(expression.getName());
		if(var.isPresent())
			return Optional.of(var.get().getType());
		return Optional.absent();
	}
	
	public static Optional<BaseType> evaluatesTo(ArrayLocation expression, Scope scope){
		Optional<FieldDescriptor> var = scope.getFromScope(expression.getName());
		if(var.isPresent() && var.get().getLength().isPresent())
			return Optional.of(var.get().getType());
		return Optional.absent();
	}
}
