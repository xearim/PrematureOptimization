package edu.mit.compilers.ast;

public enum UnaryOperator {

    ARRAY_LENGTH("@"),
    NEGATIVE("-"),
    NOT("!");
    
    private final String symbol;
    
    private UnaryOperator(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
}
