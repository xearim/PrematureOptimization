package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import antlr.collections.AST;

import com.google.common.collect.ImmutableList;

/** Utility class for dealing with ANTLR's ASTs. */
public class NodeMaker {
    // Prohibit instantiation.
    private NodeMaker() {}

    /** Make a Program from a "program" ANTLR AST. */
    public static Program program(AST program) {
        checkChildCount(3, program);
        List<AST> children = children(program);

        List<Callout> callouts = callouts(children.get(0));
        List<FieldDescriptor> globals = fieldDescriptors(children.get(1));
        List<Method> methods = methods(children.get(2));

        return new Program(callouts, globals, methods);
    }

    /** Make some Callouts from a "callouts" ANTLR AST. */
    public static List<Callout> callouts(AST callouts) {
        // TODO(jasonpr): Implement.
        return null;
    }

    /** Make some FieldDescriptors from a "field_decls" ANTLR AST. */
    public static List<FieldDescriptor> fieldDescriptors(AST field_decls) {
        // TODO(jasonpr): Implement.
        return null;
    }

    /** Make some Methods from a "method_decls" ANTLR AST. */
    public static List<Method> methods(AST method_decls) {
        // TODO(jasonpr): Implement.
        return null;
    }

    private static void checkChildCount(int min, int max, AST astNode) {
        checkArgument(min <= astNode.getNumberOfChildren());
        checkArgument(max >= astNode.getNumberOfChildren());
    }

    private static void checkChildCount(int expected, AST astNode) {
        checkArgument(expected == astNode.getNumberOfChildren());
    }

    private static List<AST> children(AST astNode) {
        ImmutableList.Builder<AST> builder = ImmutableList.builder();
        for (AST child = astNode.getFirstChild(); child != null; child = child.getNextSibling()) {
            builder.add(child);
        }
        return builder.build();
    }

}
