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

program : (callout_decl)* (field_decl)* (method_decl)* EOF;

protected
callout_decl : CALLOUT ID SEMICOLON;

protected
field_decl : type single_field_name (COMMA single_field_name)* SEMICOLON;

protected
single_field_name : (ID (L_SQ_BRACKET INT_LITERAL R_SQ_BRACKET)?);

protected
type : INT | BOOLEAN;

protected
method_decl :
	(
		(type | VOID)
		ID
		L_PAREN
		((type ID) (COMMA type ID)*)?
		R_PAREN
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
		| method_call
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
method_call :
	(
		method_name
		L_PAREN
		(method_call_arg (COMMA method_call_arg)*)?
		R_PAREN
		SEMICOLON
	);

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
// TODO(jasonpr): Finish.
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
method_name : ID;

protected
method_call_arg : (expr | STRING);

protected
location : ID (L_SQ_BRACKET expr R_SQ_BRACKET)?;

protected
assign_op : (EQ_OP | PLUS_EQ_OP | MINUS_EQ_OP);

protected
// TODO(jasonpr): Fix me!
expr : ID;
