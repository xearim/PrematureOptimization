package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

public enum BaseType {
    INTEGER,
    BOOLEAN,
    VOID,
    // Callouts return wildcards, so they can be used anywhere.
    WILDCARD;

    public boolean isA(BaseType requiredType) {
        // WILDCARD is a ..., but nothing ever is a WILDCARD.
        checkArgument(requiredType != WILDCARD, "Nothing is EVER a wildcard!");
        switch (this) {
            case BOOLEAN:
                return (requiredType == BOOLEAN);
            case INTEGER:
                return (requiredType == INTEGER);
            case VOID:
                return (requiredType == VOID);
            case WILDCARD:
                return (requiredType == BOOLEAN || requiredType == INTEGER);
            default:
                throw new AssertionError("Unexpected BaseType " + requiredType);
        }
    }
}
