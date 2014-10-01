package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.ARRAY_FIELD_DECL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BLOCK;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BOOLEAN;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BREAK;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.CALLOUTS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.CONTINUE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.FIELD_DECLS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.FOR;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.ID;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.IF;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.INT;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.INT_LITERAL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.METHOD_CALL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.METHOD_DECLS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.MINUS_EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.PLUS_EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.RETURN;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.SIGNATURE_ARG;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.SIGNATURE_ARGS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.STATEMENTS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.VOID;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.WHILE;

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
        checkType(callouts, CALLOUTS);
        ImmutableList.Builder<Callout> builder = ImmutableList.builder();
        for (AST child : children(callouts)) {
            checkType(child, ID);
            builder.add(new Callout(child.getText()));
        }
        return builder.build();
    }

    /** Make some FieldDescriptors from a "field_decls" ANTLR AST. */
    public static List<FieldDescriptor> fieldDescriptors(AST fieldDecls) {
        checkType(fieldDecls, FIELD_DECLS);
        ImmutableList.Builder<FieldDescriptor> builder = ImmutableList.builder();
        for (AST child : children(fieldDecls)) {
            builder.add(fieldDescriptor(child));
        }
        return builder.build();
    }

    /** Make some Methods from a "method_decls" ANTLR AST. */
    public static List<Method> methods(AST methodDecls) {
        checkType(methodDecls, METHOD_DECLS);
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
        if (namedField.getType() == ID) {
            String name = namedField.getText();
            return new FieldDescriptor(name, line, column, type);
        } else if (namedField.getType() == ARRAY_FIELD_DECL) {
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
        checkType(id, ID);
        return id.getText();
    }

    public static int intFromLiteral(AST intLiteral) {
        checkChildCount(0, intLiteral);
        checkType(intLiteral, INT_LITERAL);
        // TODO(jasonpr): Do a range check!
        return Integer.parseInt(intLiteral.getText());
    }

    public static BaseType baseType(AST typeNode) {
        if (typeNode.getType() == INT) {
            return BaseType.INTEGER;
        } else if (typeNode.getType() == BOOLEAN) {
            return BaseType.BOOLEAN;
        } else {
            throw new AssertionError("Unexpected type node " + typeNode);
        }
    }

    public static Method method(AST method) {
        checkType(method, ID);
        checkChildCount(3, method);

        List<AST> children = children(method);
        ReturnType returnType = returnType(children.get(0));
        List<FieldDescriptor> parameters = parameterDescriptors(children.get(1));
        Block body = block(children.get(2));
        String name = method.getText();

        return new Method(name, returnType, parameters, body);
    }

    public static ReturnType returnType(AST returnType) {
        checkChildCount(0, returnType);
        if (returnType.getType() == INT) {
            return ReturnType.fromBaseType(BaseType.INTEGER);
        } else if (returnType.getType() == BOOLEAN) {
            return ReturnType.fromBaseType(BaseType.BOOLEAN);
        } else if (returnType.getType() == VOID) {
            return ReturnType.fromVoid();
        } else {
            throw new AssertionError("Got unexpected return type node " + returnType);
        }
    }

    public static List<FieldDescriptor> parameterDescriptors(AST signatureArgs) {
        checkType(signatureArgs, SIGNATURE_ARGS);
        ImmutableList.Builder<FieldDescriptor> builder = ImmutableList.builder();
        for (AST signatureArg : children(signatureArgs)) {
            builder.add(parameterDescriptor(signatureArg));
        }
        return builder.build();
    }

    public static FieldDescriptor parameterDescriptor(AST signatureArg) {
        checkType(signatureArg, SIGNATURE_ARG);

        int line = signatureArg.getLine();
        int column = signatureArg.getColumn();

        checkChildCount(1, signatureArg);
        AST nameNode = signatureArg.getFirstChild();

        checkType(nameNode, ID);
        String name = nameNode.getText();

        checkChildCount(1, nameNode);
        BaseType type = baseType(nameNode.getFirstChild());

        return new FieldDescriptor(name, line, column, type);
    }

    public static Block block(AST block) {
        checkType(block, BLOCK);
        checkChildCount(2, block);

        List<AST> children = children(block);
        List<FieldDescriptor> locals = fieldDescriptors(children.get(0));
        List<Statement> statements = statements(children.get(1));

        // TODO(jasonpr): Remove the name parameter of Block!
        return new Block(null, locals, statements);
    }

    public static List<Statement> statements(AST statements) {
        checkType(statements, STATEMENTS);

        ImmutableList.Builder<Statement> builder = ImmutableList.builder();
        List<AST> children = children(statements);
        for (AST statement : children) {
            builder.add(statement(statement));
        }
        return builder.build();
    }

    private enum StatementType {
        ASSIGNMENT, METHOD_CALL, IF, FOR, WHILE, RETURN, BREAK, CONTINUE;
    }

    public static Statement statement(AST statement) {
        StatementType type = statementType(statement);
        switch (type) {
        case ASSIGNMENT:
            return assignment(statement);
        case BREAK:
            return methodCall(statement);
        case CONTINUE:
            return ifStatement(statement);
        case FOR:
            return forLoop(statement);
        case IF:
            return whileLoop(statement);
        case METHOD_CALL:
            return returnStatement(statement);
        case RETURN:
            return breakStatement(statement);
        case WHILE:
            return continueStatement(statement);
        default:
            throw new AssertionError("Unexpected StatementType " + type);
        }
    }

    private static StatementType statementType(AST statement) {
        switch (statement.getType()) {
            case EQ_OP: /* fall-through */
            case PLUS_EQ_OP: /* fall-through */
            case MINUS_EQ_OP:
                return StatementType.ASSIGNMENT;
            case METHOD_CALL:
                return StatementType.METHOD_CALL;
            case IF:
                return StatementType.IF;
            case FOR:
                return StatementType.FOR;
            case WHILE:
                return StatementType.WHILE;
            case RETURN:
                return StatementType.RETURN;
            case BREAK:
                return StatementType.BREAK;
            case CONTINUE:
                return StatementType.CONTINUE;
            default:
                throw new AssertionError("Non-statement type for " + statement);
        }
    }

    public static Assignment assignment(AST assignment) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static MethodCall methodCall(AST methodCall) {
        return null;
    }

    public static IfStatement ifStatement(AST ifStatement) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static ForLoop forLoop(AST forLoop) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static WhileLoop whileLoop(AST whileLoop) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static ReturnStatement returnStatement(AST returnStatement) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static BreakStatement breakStatement(AST breakStatement) {
        // TODO(jasonpr): Implement.
        return null;
    }

    public static ContinueStatement continueStatement(AST continueStatement) {
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
