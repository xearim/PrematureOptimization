package edu.mit.compilers.semantics.errors;

import edu.mit.compilers.ast.LocationDescriptor;

public class NonPositiveArrayLengthSemanticError implements SemanticError {
    private final static String ERRORNAME = "NonPositiveArrayLengthSemanticError";
    private final LocationDescriptor ld;
    private final String programName;
    private final String varName;
    private final String index;
    
    public NonPositiveArrayLengthSemanticError(LocationDescriptor ld,
            String progName, String varName, String index) {
        this.ld = ld;
        this.programName = progName;
        this.varName = varName;
        this.index = index;
    }

    @Override
    public String generateErrorMessage() {
        // TODO Auto-generated method stub
        String.format("%s: %s %s; Invalid array length of %s specified for %s",
                ERRORNAME, this.programName, genLocationString(this.ld),
                this.index, this.varName);
        return null;
    }
    
    private String genLocationString(LocationDescriptor ld) {
        return String.format("%d:%d", ld.lineNo(), ld.colNo());
    }

}
