package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ast.*;
import edu.mit.semantics.errors.MissingMainSemanticError;
import edu.mit.semantics.errors.SemanticError;

/**
 * Checks for the following rule:
 * 
 * 3) The program contains a definition for a method called main that has no parameters
 */
public class MissingMainSemanticCheck implements SemanticCheck {
    Program prog;
    List<SemanticError> errors = new ArrayList<SemanticError>();

    public MissingMainSemanticCheck(Program prog) {
        this.prog = prog;
    }

    @Override
    public List<SemanticError> doCheck() {
        // Get methods
        @SuppressWarnings("unchecked")
        Iterable<Method> methods = (Iterable<Method>) this.prog.getMethods().getChildren();

        for (Method method : methods) {
            // find methods called main
            if (method.getName().equals("main")) {
                // check to see if it has no parameters
                if (0 == method.getSignature().size()) {
                    /*
                     * returns List<SemanticErrors> of size 0
                     * may want to return null instead
                     */
                    return errors;  
                }
            }
        }

        // Only one instance of this error
        errors.add(new MissingMainSemanticError());
        return errors;
    }

}
