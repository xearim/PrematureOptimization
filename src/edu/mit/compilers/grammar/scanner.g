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
  TRUE="true";
  FALSE="false";
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

LCURLY options { paraphrase = "{"; } : "{";
RCURLY options { paraphrase = "}"; } : "}";

protected
ALPHA : ('a'..'z' | 'A'..'Z');

protected
DIGIT : ('0' .. '9');

ID options { paraphrase = "an identifier"; } : 
  ('a'..'z' | 'A'..'Z')+;

// Note that here, the {} syntax allows you to literally command the lexer
// to skip mark this token as skipped, or to advance to the next line
// by directly adding Java commands.
WS_ : (' ' | '\n' {newline();}) {_ttype = Token.SKIP; };
SL_COMMENT : "//" (~'\n')* '\n' {_ttype = Token.SKIP; newline (); };

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
NOESCAPE_PUNCTUATION :
	(
		'!'
		| '#'..'&'
		| '('..'/'
		| ':'..'@'
		| '['
		| ']'..'`'
		| '{'..'~'
	);

protected
CHAR_NAME : (ESC | SPACE | NOESCAPE_PUNCTUATION | DIGIT | ALPHA);
