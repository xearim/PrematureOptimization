package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import antlr.collections.AST;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.grammar.DecafParserTokenTypes;

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
        checkType(callouts, DecafParserTokenTypes.CALLOUTS);
        ImmutableList.Builder<Callout> builder = ImmutableList.builder();
        for (AST child : children(callouts)) {
            checkType(child, DecafParserTokenTypes.ID);
            builder.add(new Callout(child.getText()));
        }
        return builder.build();
    }

    /** Make some FieldDescriptors from a "field_decls" ANTLR AST. */
    public static List<FieldDescriptor> fieldDescriptors(AST fieldDecls) {
        checkType(fieldDecls, DecafParserTokenTypes.FIELD_DECLS);
        ImmutableList.Builder<FieldDescriptor> builder = ImmutableList.builder();
        for (AST child : children(fieldDecls)) {
            builder.add(fieldDescriptor(child));
        }
        return builder.build();
    }

    /** Make some Methods from a "method_decls" ANTLR AST. */
    public static List<Method> methods(AST methodDecls) {
        checkType(methodDecls, DecafParserTokenTypes.METHOD_DECLS);
        ImmutableList.Builder<Method> builder = ImmutableList.builder();
        for (AST child : children(methodDecls)) {
            builder.add(method(child));
        }
        return builder.build();
    }

    /** Make a FieldDescriptor from a "field_decl" ANTLR AST. */
    public static FieldDescriptor fieldDescriptor(AST fieldDecl) {
        BaseType type = baseType(fieldDecl);
        int line = fieldDecl.getLine();
        int column = fieldDecl.getColumn();

        checkChildCount(1, fieldDecl);
        AST namedField = fieldDecl.getFirstChild();
        if (namedField.getType() == DecafParserTokenTypes.ID) {
            String name = namedField.getText();
            return new FieldDescriptor(name, line, column, type);
        } else if (namedField.getType() == DecafParserTokenTypes.ARRAY_FIELD_DECL) {
            checkChildCount(2, namedField);
            List<AST> arrayInfo = children(namedField);
            String name = stringFromId(arrayInfo.get(0));
            // TODO(jasonpr): Decide whether we should store this as a String in
            // FieldDescriptor.
            int length = intFromLiteral(arrayInfo.get(1));
            return new FieldDescriptor(name, length, line, column, type);
        } else {
            throw new AssertionError("AST " + fieldDecl + " is not an ID or an ARRAY_FIELD_DECL.");
        }
    }

    public static String stringFromId(AST id) {
        checkChildCount(0, id);
        checkType(id, DecafParserTokenTypes.ID);
        return id.getText();
    }

    public static int intFromLiteral(AST intLiteral) {
        checkChildCount(0, intLiteral);
        checkType(intLiteral, DecafParserTokenTypes.INT_LITERAL);
        // TODO(jasonpr): Do a range check!
        return Integer.parseInt(intLiteral.getText());
    }

    public static BaseType baseType(AST typeNode) {
        if (typeNode.getType() == DecafParserTokenTypes.INT) {
            return BaseType.INTEGER;
        } else if (typeNode.getType() == DecafParserTokenTypes.BOOLEAN) {
            return BaseType.BOOLEAN;
        } else {
            throw new AssertionError("Unexpected type node " + typeNode);
        }
    }

    public static Method method(AST method) {
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

    private static void checkType(AST astNode, int... expected) {
        for (int type : expected) {
            if (type == astNode.getType()) {
                return;
            }
        }
        throw new AssertionError("AST " + astNode + " does not have a type in " + expected);
    }

    private static List<AST> children(AST astNode) {
        ImmutableList.Builder<AST> builder = ImmutableList.builder();
        for (AST child = astNode.getFirstChild(); child != null; child = child.getNextSibling()) {
            builder.add(child);
        }
        return builder.build();
    }

}
