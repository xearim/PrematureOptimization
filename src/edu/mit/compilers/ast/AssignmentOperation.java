package edu.mit.compilers.ast;

public enum AssignmentOperation {
    SET_EQUALS("="),
    PLUS_EQUALS("+="),
    MINUS_EQUALS("-=");
    
    private final String symbol;
    
    private AssignmentOperation(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
}
