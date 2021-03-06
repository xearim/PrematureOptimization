package edu.mit.compilers.ast;

/** An expr as defined in the Decaf spec.  Note, this excludes string literals. */
public interface NativeExpression extends GeneralExpression {
    public NativeExpression withReplacements(NativeExpression toReplace, NativeExpression replacement);

    /**
     * ExpressionType is an enum that allows for all the classes that implement NativeExpression
     * To be custom ordered with respect to one another for the purposes of imposing
     * a global expression order such that all instances of
     * 
     * A = B + C + D
     * 
     * Can be re-written as
     * 
     * X = B + C
     * A = X + D
     * 
     * So that all expressions can be simplified into two register operations for the purposes
     * of final register allocation and so that subexpressions can support replacement
     */
	public enum ExpressionType{
		BINARY_OPERATION(9),
		METHOD_CALL(8),
		TERNARY_OPERATION(7),
		UNARY_OPERATION(6),
		ARRAY_LOCATION(5),
		SCALAR_LOCATION(4),
		BOOLEAN_LITERAL(3),
		CHAR_LITERAL(2),
		INT_LITERAL(1),
		NATIVE_EXPRESSION(0);
		
		private int precedence;
		
		private ExpressionType(int i){
			this.precedence = i;
		}
		
		public int getPrecedence(){
			return precedence;
		}
	}
	
	public ExpressionType getType();
	
	/**
	 * CompareTo operates similarly to the CompareTo of most java classes
	 * namely, it return -1 if this < other, 0 if they are equivalent
	 * and 1 if this > other
	 * 
	 * This is intended to allow native expressions to define a total ordering
	 * where in all elements can be situated with respect to one another
	 * thus allowing us to re-order all expressions to match each other when 
	 * possible
	 */
	public int compareTo(NativeExpression other);
}
