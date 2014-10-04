package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ast.Program;
import edu.mit.semantics.errors.SemanticError;

public class SemanticChecker {
    private Program prog;
    private List<SemanticError> errors = new ArrayList<SemanticError>();

    public SemanticChecker(Program ir) {
        this.prog = ir;
    }

    public List<SemanticError> checkProgram() {
        /*
         * Implement checks
         */
        errors.addAll(new DeclaredTwiceSemanticCheck(this.prog).doCheck()); // 1
        errors.addAll(new UsedBeforeDeclaredSemanticCheck(this.prog).doCheck()); // 2
        errors.addAll(new MissingMainSemanticCheck(this.prog).doCheck()); // 3
        errors.addAll(new NonPositiveArrayLengthSemanticCheck(this.prog).doCheck()); // 4
        
        errors.addAll(new IncompatableArgumentsSemanticCheck(this.prog).doCheck()); // 7
        
        errors.addAll(new BreakContinueSemanticCheck(this.prog).doCheck()); // 23

        // The following check does (nearly) all type-related checks: 5,6, 8, 9,
        // and 11-22.
        errors.addAll(new TypesSemanticCheck(prog).doCheck());

        return errors;
    }
}
