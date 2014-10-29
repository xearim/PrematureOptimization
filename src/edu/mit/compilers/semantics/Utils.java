package edu.mit.compilers.semantics;

import java.util.List;

import edu.mit.compilers.semantics.errors.SemanticError;

public class Utils {
    private final static String HEX_START = "0x";
    private final static String HEX_DIGIT = "[0123456789ABCDEFabcdef]";

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

    /**
     * Uses Long.parseLong to check if a string is a valid signed 64-bit integer
     */
    private static boolean isDecimal(String i) {
        boolean b;
        try {
            Long.parseLong(i);
            b = true;
        } catch (NumberFormatException e) {
            b = false;
        }
        return b;
    }

    /**
     * Uses regular expressions to see if it's a hex literal with at most 16
     * hex digits.
     */
    private static boolean isHex(String i) {
        return i.matches(HEX_START + HEX_DIGIT + "{1,16}");
    }

    /**
     * Checks if the string is a 64-bit signed decimal or hexadecimal.
     *
     * @param i Any string
     * @return If the string is a valid int_literal that falls within bounds
     */
    public static boolean isWithinBounds(String i) {
        return isDecimal(i) || isHex(i);
    }

}
