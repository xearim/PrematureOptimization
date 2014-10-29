package edu.mit.compilers.semantics.errors;

public enum SemanticErrorType {
	/*
	 * 1) No identifier is declared twice in the same scope.
	 * This includes 'callout' identifiers, which exist in the global scope.
	 */
	DECLARED_TWICE,
	/*
	 * 2) No identifier is used before it is declared.
	 */
	ID_USED_BEFORE_DECLARATION,
	/*
	 * 3) The program contains a definition for a method called 'main' that has no
	 * parameters.
	 */
	NO_MAIN,
	/*
	 * 4) The INT_LITERAL in an array declaration must be greater than zero.
	 */
	NEGATIVE_INT_LITERAL_DECL,
	/*
	 * 5) The number and types of arguments in a method call (non-callout)
	 * must be the same as the number and types of the formals.
	 * i.e. the signatures must be identical  
	 */
	MISMATCH_SIGNATURES,
	/*
	 * 6) If a method is used as an expression, the method must return a
	 * result.
	 */
	MISSING_RESULT_FROM_METHOD_CALL,
	/*
	 * 7) String literals and array variables may not be used as arguments to
	 * non-callout methods.
	 */
	INVALID_SIGNATURE,
	/*
	 * 8) A 'return' statement must not have a return value unless it appears
	 * in the body of a method that is declared to return a value.
	 */
	NOT_RETURNING_VOID,
	/*
	 * 9) The expression in a 'return' statement must have the same type as the
	 * declared result type of the enclosing method definition.
	 */
	MISMATCH_RETURN_TYPE,
	/*
	 * 10) An ID used as a LOCATION must name a declared local/global variable
	 * or formal parameter.
	 */
	LOCATION_ID_NOT_DECLARED,
	/*
	 * 11) For all locations of the form ID[EXPR]
	 * a) ID must be an 'array' variable, and
	 * b) the type of EXPR must be 'int'.
	 */
	INVALID_ARRAY_LOCATION,
	/*
	 * 12) The argument of the @ operator must be an array variable.
	 */
	ARRAY_LEN_OP_INVALID_INPUT_TO,
	/*
	 * 13) The EXPR in an 'if' or a 'while' statement must have type boolean
	 */
	NONBOOLEAN_CONDITIONAL_IF,
	NONBOOLEAN_CONDITIONAL_WHILE,
	/*
	 * 14) The first EXPR in a ternary conditional expression (?:) must have
	 * type 'boolean'.
	 */
	NONBOOLEAN_CONDITIONAL_TERNARY,
	/*
	 * 15) The other two expressions in a ternary conditional expression must
	 * have the same type (integer or boolean)
	 */
	INVALID_EXPR_TERNARY,
	/*
	 * 16) the operands of ARITH_OP and REL_OP must have type 'int'.
	 */
	INVALID_ARITH_OP_OPERAND,
	INVALID_REL_OP_OPERAND,
	/*
	 * 17) The operands of EQ_OPs must have the same type, either 'int' or
	 * 'boolean'.
	 */
	MISMATCH_EQ_OP_OPERANDS,
	/*
	 * 18) The operands of COND_OPs and the operand of logical not (!) must
	 * have type boolean.
	 */
	NONBOOLEAN_COND_OPS,
	NONBOOLEAN_COND_NEGATION,
	/*
	 * 19) The LOCATION and the EXPR in an assignment, LOCATION = EXPR, must
	 * have the same type.
	 */
	MISMATCH_ASSIGNMENT_TYPES,
	/*
	 * 20) the LOCATION and the EXPR in an incrementing/decrementing
	 * assignment, LOCATION += EXPR and LOCATION -= EXPR, must be of type
	 * 'int'.
	 * 
	 */
	MISMATCH_INC_ASSIGNMENT_TYPES,
	MISMATCH_DEC_ASSIGNMENT_TYPES,
	/*
	 * 21) The initial EXPR and the ending EXPR of 'for' must have the type
	 * 'int'.
	 */
	MISMATCH_FOR_EXPR,
	/*
	 * 22) The optional upper bound on the number of iterations of the 'while'
	 * loop must be a positive integer.
	 */
	WHILE_LOOP_UPPER_BOUND_NOT_POS_INT,
	/*
	 * 23) All 'break' and 'continue' statements must be contained within the
	 * body of a 'for' or a 'while'.
	 */
	INVALID_BREAK,
	INVALID_CONTINUE,
}
