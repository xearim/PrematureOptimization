package edu.mit.compilers;

import static com.google.common.base.Preconditions.checkArgument;
import antlr.collections.AST;

/** Utility class for dealing with ANTLR's ASTs. */
public class AntlrAstUtils {
    // Prohibit instantiation.
    private AntlrAstUtils() {}

    public static void checkChildCount(int min, int max, AST astNode) {
        checkArgument(min <= astNode.getNumberOfChildren());
        checkArgument(max >= astNode.getNumberOfChildren());
    }

    public static void checkChildCount(int expected, AST astNode) {
        checkArgument(expected == astNode.getNumberOfChildren());
    }
}
