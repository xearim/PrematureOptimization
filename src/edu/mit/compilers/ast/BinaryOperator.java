package edu.mit.compilers.ast;

public enum BinaryOperator {
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVIDED_BY("/"),
    MODULO("%"),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">="),
    DOUBLE_EQUALS("=="),
    NOT_EQUALS("!="),
    AND("&&"),
    OR("||");
    
    private final String symbol;
    
    private BinaryOperator(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
}
