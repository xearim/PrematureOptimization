header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

class DecafParser extends Parser;
options
{
  importVocab = DecafScanner;
  k = 3;
  buildAST = true;
}

tokens {
	PROGRAM;
	CALLOUTS;
	FIELD_DECLS;
	METHOD_DECLS;
}

// Java glue code that makes error reporting easier.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  // Do our own reporting of errors so the parser can return a non-zero status
  // if any errors are detected.
  /** Reports if any errors were reported during parse. */
  private boolean error;

  @Override
  public void reportError (RecognitionException ex) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  @Override
  public void reportError (String s) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  public boolean getError () {
    return error;
  }

  // Selectively turns on debug mode.

  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws TokenStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws TokenStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}

program :
	(callouts field_decls method_decls EOF!)
	{ #program = #([PROGRAM, "program"], #program ); };

protected
callouts :
    (CALLOUT! ID SEMICOLON!)*
	{ #callouts = #([CALLOUTS, "callouts"], #callouts ); };

protected
field_decls :
	(field_decl)*
	{ #field_decls = #([FIELD_DECLS, "field_decls"], #field_decls ); };


// TODO(jasonpr): Determine how to hoist the types without duplicating code from "type".
protected
field_decl : ((INT^ | BOOLEAN^) single_field_name (COMMA! single_field_name)* SEMICOLON!);

protected
single_field_name : (ID (L_SQ_BRACKET INT_LITERAL R_SQ_BRACKET)?);

protected
type : INT | BOOLEAN;

protected
method_decls :
	(method_decl)*
	{ #method_decls = #([METHOD_DECLS, "method_decls"], #method_decls ); };

protected
method_decl :
	(
		(type | VOID)
		ID^
		L_PAREN!
		((type ID) (COMMA! type ID)*)?
		R_PAREN!
		block
	);

protected
block :
	(
		L_CURLY_BRACKET
		(field_decl)*
		(statement)*
		R_CURLY_BRACKET
	);

protected
statement :
	(
		assignment
		| method_call_statement
		| if_statement
		| for_loop
		| while_loop
		| return_statement
		| break_statement
		| continue_statement
	);

protected
assignment :
	(
		location
		assign_op
		expr
		SEMICOLON
	);

protected
method_call_statement : method_call SEMICOLON;

protected
if_statement :
	(
		IF
		L_PAREN expr R_PAREN
		block
		(ELSE block)?
	);

protected
for_loop :
	(
		FOR
		L_PAREN
		ID
		EQ_OP
		expr COMMA expr
		R_PAREN
		block
	);

protected
while_loop :
	(
		WHILE
		L_PAREN expr R_PAREN
		(COLON INT_LITERAL)?
		block
	);

protected
return_statement : RETURN (expr)? SEMICOLON;

protected
break_statement : BREAK SEMICOLON;

protected
continue_statement : CONTINUE SEMICOLON;

protected
method_call :
	(
		method_name
		L_PAREN
		(method_call_arg (COMMA method_call_arg)*)?
		R_PAREN
	);

protected
method_name : ID;

protected
method_call_arg : (expr | STRING);

protected
location : ID (L_SQ_BRACKET expr R_SQ_BRACKET)?;

protected
assign_op : (EQ_OP | PLUS_EQ_OP | MINUS_EQ_OP);

protected
expr :
	(
		// TODO(jasonpr): Rename all *_expr to better indicate that it's either
		// an expression of that type, or an expression containing operations
		// that bind at higher precedence.
		ternary_expr
	);

ternary_expr :
	(
		cond_or_expr
		(options {greedy=true;} : QUESTION ternary_expr COLON ternary_expr)?
	);

protected
cond_or_expr :
	(
		cond_and_expr
		// Greedily tack on conditional "or" operations.
		(options {greedy=true;} : COND_OR cond_or_expr)*
	);

protected
cond_and_expr :
	(
		equality_expr
		// Greedily tack on conditional "and" operations.
		(options {greedy=true;} : COND_AND cond_and_expr)*
	);

protected
equality_expr :
	(
		relative_expr
		// Greedily tack on equality ops.  Note that "a == b == c" is
		// syntactically valid (albeit not semantically valid).
		(options {greedy=true;} : equality_op equality_expr)*
	);

protected
equality_op : DOUBLE_EQUAL | NE;

protected
relative_expr :
	(
		added_expr
		// Greedily tack on relative ops.  Note that "a < b < c" is
		// syntactically valid (albeit not semantically valid).
		(options {greedy=true;} : relative_op relative_expr)*
	);

protected
relative_op : LT | GT | LTE | GTE;

protected
added_expr :
	(
		multiplied_expr
		// Greedily tack on additive binary ops.
		(options {greedy=true;} : additive_op  added_expr)*
	);

protected
additive_op : PLUS | MINUS;

protected
multiplied_expr :
	(
		strongest_binding_expr
		// Greedily tack on multiplicative binary ops.
		(options {greedy=true;} : multiplicative_op multiplied_expr)*
	);

protected
multiplicative_op : TIMES | DIVIDED | MODULO;

protected
strongest_binding_expr :
	(
		location
		| method_call
		| literal
		| array_length
		| inverted_expr
		| additive_inverse_expr
		| parenthesized_expr
	);

protected
literal : (INT_LITERAL | CHAR | boolean_literal);

protected
boolean_literal : (TRUE | FALSE);

protected
array_length : AT_SIGN ID;

protected
math_expr :
	(
		additive_inverse_expr
		// TODO(jasonpr): Implement.
		//| binary_operation_expr
	);

protected
additive_inverse_expr :	MINUS expr;

protected
inverted_expr : NOT_OP expr;

protected
parenthesized_expr : L_PAREN expr R_PAREN;


// TODO(jasonpr): Enable ternary!
//protected
//ternary_condition_expr :
//	(
//		expr
//		QUESTION
//		expr
//		COLON
//		expr
//	);
