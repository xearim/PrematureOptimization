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
	SIGNATURE_ARGS;
	SIGNATURE_ARG;
	BLOCK;
	STATEMENTS;
	METHOD_CALL;
	METHOD_CALL_ARGS;
	ARRAY_LOCATION;
	ARRAY_FIELD_DECL;
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
	(options {greedy=true;} : field_decl)*
	{ #field_decls = #([FIELD_DECLS, "field_decls"], #field_decls ); };


// TODO(jasonpr): Determine how to hoist the types without duplicating code from "type".
protected
field_decl : ((INT^ | BOOLEAN^) single_field_decl (COMMA! single_field_decl)* SEMICOLON!);

protected
single_field_decl : (scalar_field_decl | array_field_decl);

protected
scalar_field_decl : ID;

protected
array_field_decl :
	(ID L_SQ_BRACKET! INT_LITERAL R_SQ_BRACKET!)
	{ #array_field_decl = #([ARRAY_FIELD_DECL, "array_field_decl"], #array_field_decl ); };

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
		signature_args
		R_PAREN!
		block
	);

protected
signature_args :
	(signature_arg (COMMA! signature_arg)*)?
	{ #signature_args = #([SIGNATURE_ARGS, "signature_args"], #signature_args ); };

// TODO(jasonpr): Resolve difference between the ID -> type edges here, and
// the type -> ID edges in field_decl.
protected
signature_arg:
	(type ID^)
	{ #signature_arg = #([SIGNATURE_ARG, "signature_arg"], #signature_arg ); };

protected
block :
	(
		L_CURLY_BRACKET!
		field_decls
		statements
		R_CURLY_BRACKET!
	)
	{ #block = #([BLOCK, "block"], #block ); };

protected
statements :
	(statement)*
	{ #statements = #([STATEMENTS, "statements"], #statements); };

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
		(EQ_OP^ | PLUS_EQ_OP^ | MINUS_EQ_OP^)
		expr
		SEMICOLON!
	);

protected
method_call_statement : method_call SEMICOLON!;

protected
if_statement :
	(
		IF^
		L_PAREN! expr R_PAREN!
		block
		(ELSE! block)?
	);

protected
for_loop :
	(
		FOR^
		L_PAREN!
		ID
		EQ_OP!
		expr COMMA! expr
		R_PAREN!
		block
	);

protected
while_loop :
	(
		WHILE^
		L_PAREN! expr R_PAREN!
	    // The bound is optional, so the while_loop has either three or four children..
	    // and, perhaps counterintuitively, the optional node is NOT the final node.
	    // It's not ideal, but I'm having trouble implementing saner plans with ANTLR.
	    // TODO(jasonpr): Figure out how to implement a saner plan with ANTLR!
		(while_bound)?
		block
	);

protected
while_bound : (COLON^ INT_LITERAL);

protected
return_statement : RETURN^ (expr)? SEMICOLON!;

protected
break_statement : BREAK SEMICOLON!;

protected
continue_statement : CONTINUE SEMICOLON!;

protected
method_call :
	(
		method_name
		L_PAREN! (method_call_args)? R_PAREN!
	)
	{ #method_call = #([METHOD_CALL, "method_call"], #method_call); };


protected
method_call_args :
	method_call_arg (COMMA! method_call_arg)*
	{ #method_call_args = #([METHOD_CALL_ARGS, "method_call_args"], #method_call_args ); };

protected
method_name : ID;

protected
method_call_arg : (expr | STRING);

protected
location : (scalar_location | array_location);

protected
scalar_location : ID;

protected
array_location :
	(ID L_SQ_BRACKET! expr R_SQ_BRACKET!)
	{ #array_location = #([ARRAY_LOCATION, "array_location"], #array_location ); };

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
		(options {greedy=true;} : QUESTION! ternary_expr COLON! ternary_expr)?
	);

protected
cond_or_expr :
	(
		cond_and_expr
		// Greedily tack on conditional "or" operations.
		(options {greedy=true;} : COND_OR^ cond_and_expr)*
	);

protected
cond_and_expr :
	(
		equality_expr
		// Greedily tack on conditional "and" operations.
		(options {greedy=true;} : COND_AND^ equality_expr)*
	);

protected
equality_expr :
	(
		relative_expr
		// Greedily tack on equality ops.  Note that "a == b == c" is
		// syntactically valid (albeit not semantically valid).
		(options {greedy=true;} : (DOUBLE_EQUAL^ | NE^) relative_expr)*
	);

protected
relative_expr :
	(
		added_expr
		// Greedily tack on relative ops.  Note that "a < b < c" is
		// syntactically valid (albeit not semantically valid).
		(options {greedy=true;} : (LT^ | GT^ | LTE^ | GTE^) added_expr)*
	);

protected
added_expr :
	(
		multiplied_expr
		// Greedily tack on additive binary ops.
		(options {greedy=true;} : (PLUS^ | MINUS^)  multiplied_expr)*
	);

protected
multiplied_expr :
	(
		strongest_binding_expr
		// Greedily tack on multiplicative binary ops.
		(options {greedy=true;} : (TIMES^ | DIVIDED^ | MODULO^) strongest_binding_expr)*
	);

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
array_length : AT_SIGN^ ID;

protected
math_expr :
	(
		additive_inverse_expr
		// TODO(jasonpr): Implement.
		//| binary_operation_expr
	);

protected
additive_inverse_expr :	MINUS^ expr;

protected
inverted_expr : NOT_OP^ expr;

protected
parenthesized_expr : L_PAREN! expr R_PAREN!;


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
