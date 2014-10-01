package edu.mit.compilers.ir;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.*;

public class EvaluateCheck {

	public static Optional<BaseType> evaluatesTo(NativeExpression expression, Scope scope){
		return Optional.absent();
	}
	
	public static Optional<BaseType> evaluatesTo(BinaryOperation expression, Scope scope){
		switch(expression.getOperation()){
		/* fall-through */
		case AND:
		case OR:
		case DOUBLE_EQUALS:
		case NOT_EQUALS:
		case GREATER_THAN_OR_EQUAL:
		case GREATER_THAN:
		case LESS_THAN_OR_EQUAL:
		case LESS_THAN:
			return Optional.of(BaseType.BOOLEAN);
		/* fall-through */
		case DIVIDED_BY:
		case MINUS:
		case MODULO:
		case PLUS:
		case TIMES:
			return Optional.of(BaseType.INTEGER);
		// Should never reach here
		default:
			throw new AssertionError("Binary Operations are not valid for operator " + expression.getOperation());
		}
	}
	
	public static Optional<BaseType> evaluatesTo(UnaryOperation expression, Scope scope){
		switch(expression.getOperation()){
		case ARRAY_LENGTH:
			return Optional.of(BaseType.INTEGER);
		case NEGATIVE:
			return Optional.of(BaseType.INTEGER);
		case NOT:
			return Optional.of(BaseType.BOOLEAN);
		default:
			throw new AssertionError("Binary Operations are not valid for operator " + expression.getOperation());
		}
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
	
	public static Optional<BaseType> evaluatesTo(MethodCall expression, Scope scope){
		return Optional.of(BaseType.VOID);
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
