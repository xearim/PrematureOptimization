package edu.mit.semantics.errors;

import edu.mit.compilers.ast.LocationDescriptor;

public class IncompatableArgumentsSemanticError implements SemanticError {
    private final static String ERRORNAME="IncompatableArgumentsSemanticError";
    private final String name;
    private final LocationDescriptor ld;

    public IncompatableArgumentsSemanticError(String name, LocationDescriptor ld) {
        this.name = name;
        this.ld = ld;
    }

    @Override
    public String generateErrorMessage() {
        return String.format("%s: %s %s; %s is not a valid parameter for a non-callout method",
                ERRORNAME, this.ld.getFileName(), generateLocationString(), this.name);
    }

    private String generateLocationString() {
        return String.format("%d:%d", this.ld.lineNo(), this.ld.colNo());
    }
}
