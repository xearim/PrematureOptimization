package edu.mit.compilers;

import antlr.CommonAST;
import antlr.Token;

public class AntlrASTWithLines extends CommonAST {
    private int line = 0;
    private int column = 0;
    
    public void initialize(Token tok) {
        super.initialize(tok);
        line=tok.getLine();
        column=tok.getColumn();
    }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}

