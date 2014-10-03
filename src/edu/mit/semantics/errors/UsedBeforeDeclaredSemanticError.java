package edu.mit.semantics.errors;

import edu.mit.compilers.ast.LocationDescriptor;

public class UsedBeforeDeclaredSemanticError implements SemanticError {
    private final static String ERRORNAME="UsedBeforeDeclaredSemanticError";
    private final String name;
    private final LocationDescriptor ld;

    public UsedBeforeDeclaredSemanticError(String name, LocationDescriptor ld) {
        this.name = name;
        this.ld = ld;
    }

    @Override
    public String generateErrorMessage() {
        return String.format("%s: %s %s; Variable %s used before declared",
                ERRORNAME, this.ld.getFileName(), generateLocationString(), this.name);
    }

    private String generateLocationString() {
        return String.format("%d:%d", this.ld.lineNo(), this.ld.colNo());
    }
}
