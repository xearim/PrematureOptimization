package edu.mit.compilers.ast;

public enum AssignmentOperation {
    SET_EQUALS("=", true),
    PLUS_EQUALS("+=", false),
    MINUS_EQUALS("-=", false);

    private final String symbol;
    private final boolean isAbsolute;

    private AssignmentOperation(String symbol, boolean isAbsolute) {
        this.symbol = symbol;
        this.isAbsolute = isAbsolute;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns whether this assignment operation is absolute (like "=")
     * rather than relative (like "+=").
     */
    public boolean isAbsolute() {
        return isAbsolute;
    }
}
