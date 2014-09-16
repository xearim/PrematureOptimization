header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

{@SuppressWarnings("unchecked")}
class DecafScanner extends Lexer;
options
{
  k = 2;
}

tokens 
{
  "class";
  BOOLEAN="boolean";
  BREAK="break";
  CALLOUT="callout";
  CONTINUE="continue";
  ELSE="else";
  FALSE="false";
  FOR="for";
  WHILE="while";
  IF="if";
  INT="int";
  RETURN="return";
  TRUE="true";
  VOID="void";
}

// Selectively turns on debug tracing mode.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws CharStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws CharStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}


L_PAREN options { paraphrase = "("; } : "(";
R_PAREN options { paraphrase = ")"; } : ")";

L_SQ_BRACKET options { paraphrase = "["; } : "[";
R_SQ_BRACKET options { paraphrase = "]"; } : "]";

L_CURLY_BRACKET options { paraphrase = "{"; } : "{";
R_CURLY_BRACKET options { paraphrase = "}"; } : "}";

// Operators that introduce lookahead issues, or that are related to those
// operators.
// TODO(jasonpr): Make ANTLR switch up the type, depending upon which
// one matches.

EQ_OP : "=";
PLUS_EQ_OP : "+=";
MINUS_EQ_OP : "-=";
PLUS : "+";
MINUS : "-";
TIMES : "*";
DIVIDED : "/";
MODULO : "%";
LT : "<";
GT : ">";
LTE : "<=";
GTE : ">=";
NE : "!=";
DOUBLE_EQUAL : "==";
NOT_OP : '!';
COND_OP : "&&" | "||";

SEMICOLON : ';';

COLON : ':';

COMMA : ',';

QUESTION : '?';

AT_SIGN : '@';

protected
ALPHA : ('a'..'z' | 'A'..'Z' | '_');

protected
DIGIT : ('0' .. '9');

protected
ALPHA_NUM : (ALPHA | DIGIT);

// TODO(jasonpr): Add test that fails on "1badname".
ID options { paraphrase = "an identifier"; } : 
  ALPHA (ALPHA_NUM)*;

// Note that here, the {} syntax allows you to literally command the lexer
// to skip mark this token as skipped, or to advance to the next line
// by directly adding Java commands.
// TODO(jasonpr): Consider adding \r, or other esoteric whitespaces.
WS_ : (' ' | '\t' | '\n' {newline();}) {_ttype = Token.SKIP; };
SL_COMMENT : "//" (~'\n')* '\n' {_ttype = Token.SKIP; newline (); };

protected
HEX_DIGIT :
	(
		DIGIT
		| 'a'..'f'
		| 'A'..'F'
	);

protected
HEX_LITERAL : "0x" (HEX_DIGIT)+;

protected
DECIMAL_LITERAL : (DIGIT)+;

INT_LITERAL: DECIMAL_LITERAL | HEX_LITERAL;

CHAR : '\'' CHAR_NAME '\'';
STRING : '"' (CHAR_NAME)* '"';

protected
SPACE : ' ';

protected
// Note that the backslashes are to escape characters for ANTLR.
// Each single-quote-enclosed string is a single character.
ESC :  '\\' ('"'|'\''|'\\'|'t'|'n');

protected
// Punctuation that doesn't ever need escaping.
// That is, ASCII punctuation that is not a single quote,
// double quote, or backslash.
// Note that an underscore is considered ALPHA, not punctuation!
NOESCAPE_PUNCTUATION :
	(
		'!'
		| '#'..'&'
		| '('..'/'
		| ':'..'@'
		| '['
		| ']'
		| '^'
		| '`'
		| '{'..'~'
	);

protected
CHAR_NAME : (ESC | SPACE | NOESCAPE_PUNCTUATION | DIGIT | ALPHA);
