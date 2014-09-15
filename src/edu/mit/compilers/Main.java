package edu.mit.compilers;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintStream;

import antlr.CharStreamException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import edu.mit.compilers.grammar.DecafParser;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.grammar.DecafScanner;
import edu.mit.compilers.grammar.DecafScannerTokenTypes;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.tools.CLI.Action;

class Main {
  public static void main(String[] args) {
    try {
      // Setup in and out files.
      CLI.parse(args, new String[0]);
      InputStream inputStream = args.length == 0 ?
          System.in : new java.io.FileInputStream(CLI.infile);
      PrintStream outputStream = CLI.outfile == null ? System.out : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));

      // Parse or scan.
      if (CLI.target == Action.SCAN) {
        scan(inputStream, outputStream);
      } else if (CLI.target == Action.PARSE ||
                 CLI.target == Action.DEFAULT) {
        parse(inputStream, outputStream);
      }
    } catch(Exception e) {
      // An unrecoverable error occurred.
      System.err.println(CLI.infile+" "+e);
    }
  }

  // TODO(jasonpr): Javadoc.
  private static void scan(InputStream inputStream, PrintStream outputStream) throws CharStreamException {
    DecafScanner scanner =
        new DecafScanner(new DataInputStream(inputStream));
    scanner.setTrace(CLI.debug);
    Token token;
    boolean done = false;
    while (!done) {
      try {
        for (token = scanner.nextToken();
             token.getType() != DecafParserTokenTypes.EOF;
             token = scanner.nextToken()) {
          String type = "";
          String text = token.getText();
          switch (token.getType()) {
           // TODO: add strings for the other types here...
           case DecafScannerTokenTypes.ID:
             type = " IDENTIFIER";
             break;
           case DecafScannerTokenTypes.CHAR:
             type = " CHARLITERAL";
             break;
           case DecafScannerTokenTypes.STRING:
             type = " STRINGLITERAL";
             break;
           case DecafScannerTokenTypes.TRUE:  /* fall-through */
           case DecafScannerTokenTypes.FALSE:
             type = " BOOLEANLITERAL";
             break;
          }
          outputStream.println(token.getLine() + type + " " + text);
        }
        done = true;
      } catch(Exception e) {
        // print the error:
        System.err.println(CLI.infile + " " + e);
        scanner.consume();
      }
    }
  }

  // TODO(jasonpr): Javadoc.
  private static void parse(InputStream inputStream, PrintStream outputStream) throws RecognitionException, TokenStreamException {
    DecafScanner scanner =
        new DecafScanner(new DataInputStream(inputStream));
    DecafParser parser = new DecafParser(scanner);
    parser.setTrace(CLI.debug);
    parser.program();
    if(parser.getError()) {
      System.exit(1);
    }
  }

}
