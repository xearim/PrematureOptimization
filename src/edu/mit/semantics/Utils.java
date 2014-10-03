package edu.mit.semantics;

import java.util.List;

import edu.mit.semantics.errors.SemanticError;

public class Utils {

    private Utils() {}

    // TOOD(jasonpr): Make this an instance method of an ErrorAccumulator class.
    /**
     * Add an error to an error accumulator if a condition is false.
     *
     * @param condition The condition that is false in case of an error.
     * @param errorAccumulator The error accumulator to which an SemanticError should be added.
     * @param message The error message, with String.format-like arguments.
     * @param messageArgs The arguments to the error message formatter.
     */
    public static void check(boolean condition, List<SemanticError> errorAccumulator,
            final String message, final Object... messageArgs) {
        if (!condition) {
            errorAccumulator.add(new SemanticError() {
                @Override
                public String generateErrorMessage() {
                    return String.format(message, messageArgs);
                }
            });
        }
    }

}
