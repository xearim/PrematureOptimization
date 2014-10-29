package edu.mit.compilers.semantics;

import java.io.PrintStream;
import java.util.List;

import edu.mit.compilers.semantics.errors.SemanticError;

public class ErrorPrinter {
    private final List<SemanticError> errors;
    private final PrintStream ps;
    
    public ErrorPrinter(List<SemanticError> errors, PrintStream ps) {
        this.errors = errors;
        this.ps = ps;
    }
    
    public void print() {
        for (SemanticError error : errors) {
            ps.println(error.generateErrorMessage());
        }
    }
}
