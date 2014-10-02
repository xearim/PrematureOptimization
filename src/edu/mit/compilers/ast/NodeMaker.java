package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.ARRAY_FIELD_DECL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.ARRAY_LOCATION;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.AT_SIGN;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BLOCK;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BOOLEAN;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.BREAK;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.CALLOUTS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.CHAR;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.COND_AND;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.COND_OR;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.CONTINUE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.DIVIDED;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.DOUBLE_EQUAL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.FALSE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.FIELD_DECLS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.FOR;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.GT;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.GTE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.ID;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.IF;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.INT;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.INT_LITERAL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.LT;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.LTE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.METHOD_CALL;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.METHOD_CALL_ARGS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.METHOD_DECLS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.MINUS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.MINUS_EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.MODULO;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.NE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.NOT_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.PLUS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.PLUS_EQ_OP;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.QUESTION;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.RETURN;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.SIGNATURE_ARG;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.SIGNATURE_ARGS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.STATEMENTS;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.STRING;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.TIMES;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.TRUE;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.VOID;
import static edu.mit.compilers.grammar.DecafParserTokenTypes.WHILE;

import java.util.List;
import java.util.Map;

import antlr.collections.AST;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** Utility class for dealing with ANTLR's ASTs. */
public class NodeMaker {

    private static final Map<Integer, UnaryOperator> UNARY_OPERATORS;
    private static final Map<Integer, BinaryOperator> BINARY_OPERATORS;

    static {
        UNARY_OPERATORS = ImmutableMap.of(
                AT_SIGN, UnaryOperator.ARRAY_LENGTH,
                NOT_OP, UnaryOperator.NOT,
                MINUS, UnaryOperator.NEGATIVE);

        BINARY_OPERATORS = ImmutableMap.<Integer, BinaryOperator>builder()
                .put(TIMES, BinaryOperator.TIMES)
                .put(DIVIDED, BinaryOperator.DIVIDED_BY)
                .put(MODULO, BinaryOperator.MODULO)
                .put(PLUS, BinaryOperator.PLUS)
                .put(MINUS, BinaryOperator.MINUS)
                .put(LT, BinaryOperator.LESS_THAN)
                .put(GT, BinaryOperator.GREATER_THAN)
                .put(LTE, BinaryOperator.LESS_THAN_OR_EQUAL)
                .put(GTE, BinaryOperator.GREATER_THAN_OR_EQUAL)
                .put(DOUBLE_EQUAL, BinaryOperator.DOUBLE_EQUALS)
                .put(NE, BinaryOperator.NOT_EQUALS)
                .put(COND_OR, BinaryOperator.OR)
                .put(COND_AND, BinaryOperator.AND)
                .build();
    }

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
        AssignmentOperation operation;
        if (assignment.getType() == EQ_OP) {
            operation = AssignmentOperation.SET_EQUALS;
        } else if (assignment.getType() == PLUS_EQ_OP) {
            operation = AssignmentOperation.PLUS_EQUALS;
        } else if (assignment.getType() == MINUS_EQ_OP) {
            operation = AssignmentOperation.MINUS_EQUALS;
        } else {
            throw new AssertionError("Unexpected type for assignment " + assignment);
        }

        checkChildCount(2, assignment);
        List<AST> children = children(assignment);
        return new Assignment(location(children.get(0)), operation,
                nativeExpression(children.get(1)));
    }

    public static MethodCall methodCall(AST methodCall) {
        checkType(methodCall, METHOD_CALL);
        checkChildCount(1, 2, methodCall);

        List<AST> children = children(methodCall);
        String methodName = stringFromId(children.get(0));
        List<GeneralExpression> parameterValues = (children.size() == 1)
                ? ImmutableList.<GeneralExpression>of()
                : methodCallArgs(children.get(1));

        return new MethodCall(methodName, parameterValues);
    }

    public static IfStatement ifStatement(AST ifStatement) {
        checkChildCount(2, 3, ifStatement);
        List<AST> children = children(ifStatement);
        NativeExpression condition = nativeExpression(children.get(0));
        Block thenBlock = block(children.get(1));
        if (children.size() == 2) {
            // It's just the condition and the then block.
            return IfStatement.ifThen(condition, thenBlock);
        } else {
            // There's an else block, too!
            Block elseBlock = block(children.get(2));
            return IfStatement.ifThenElse(condition, thenBlock, elseBlock);
        }
    }

    public static ForLoop forLoop(AST forLoop) {
        checkChildCount(4, forLoop);

        List<AST> children = children(forLoop);
        ScalarLocation loopVariable = scalarLocation(children.get(0));
        NativeExpression rangeStart = nativeExpression(children.get(1));
        NativeExpression rangeEnd = nativeExpression(children.get(2));
        Block body = block(children.get(3));

        return new ForLoop(loopVariable, rangeStart, rangeEnd, body);
    }

    public static WhileLoop whileLoop(AST whileLoop) {
        checkChildCount(2, 3, whileLoop);

        List<AST> children = children(whileLoop);
        NativeExpression condition = nativeExpression(children.get(0));

        if (children.size() == 2) {
            // There is no bound on the while loop.
            return WhileLoop.simple(condition, block(children.get(1)));
        } else {
            // There is a bound on the while loop.
            IntLiteral maxRepetitions = intLiteral(children.get(1));
            Block body = block(children.get(2));
            return WhileLoop.limited(condition, maxRepetitions, body);
        }
    }

    public static ReturnStatement returnStatement(AST returnStatement) {
        checkType(returnStatement, RETURN);
        checkChildCount(0, 1, returnStatement);

        List<AST> children = children(returnStatement);
        if (children.size() == 0) {
            return ReturnStatement.ofVoid();
        } else {
            return ReturnStatement.of(nativeExpression(children.get(1)));
        }
    }

    public static NativeExpression nativeExpression(AST nativeExpression) {
        NativeExpressionType type = nativeExpressionType(nativeExpression);
        switch (type) {
            case LOCATION:
                return location(nativeExpression);
            case METHOD_CALL:
                return methodCall(nativeExpression);
            case LITERAL:
                return literal(nativeExpression);
            case UNARY_OPERATION:
                return unaryOperation(nativeExpression);
            case BINARY_OPERATION:
                return binaryOperation(nativeExpression);
            case TERNARY_OPERATION:
                return ternaryOperation(nativeExpression);
            case UNARY_MINUS:
                return unaryMinus(nativeExpression);
            default:
                throw new AssertionError("Unexpected StatementType " + type);
        }
    }

    private enum NativeExpressionType {
        LOCATION,
        METHOD_CALL,
        LITERAL,
        UNARY_OPERATION,
        BINARY_OPERATION,
        TERNARY_OPERATION,
        UNARY_MINUS,
    }

    private static NativeExpressionType nativeExpressionType(AST expr) {
        switch (expr.getType()) {
            case METHOD_CALL:
                return NativeExpressionType.METHOD_CALL;
            case CHAR:  /* fall-through */
            case INT_LITERAL:  /* fall-through */
            case TRUE:  /* fall-through */
            case FALSE:
                return NativeExpressionType.LITERAL;
            case AT_SIGN:  /* fall-through */
            case NOT_OP:
                return NativeExpressionType.UNARY_OPERATION;
            case TIMES:  /* fall-through */
            case DIVIDED:  /* fall-through */
            case MODULO:  /* fall-through */
            case PLUS:  /* fall-through */
            case LT:  /* fall-through */
            case GT:  /* fall-through */
            case LTE:  /* fall-through */
            case GTE:  /* fall-through */
            case DOUBLE_EQUAL:  /* fall-through */
            case NE:  /* fall-through */
            case COND_AND:  /* fall-through */
            case COND_OR:
                return NativeExpressionType.BINARY_OPERATION;
            case QUESTION:
                return NativeExpressionType.TERNARY_OPERATION;
            default:
                // We must handle two special cases outside the switch
                // statement.
                break;
        }
        if (isLocation(expr)) {
            return NativeExpressionType.LOCATION;
        }
        if (expr.getType() == MINUS) {
            int childCount = expr.getNumberOfChildren();
            if (childCount == 1) {
                return NativeExpressionType.UNARY_MINUS;
            } else if (childCount == 2) {
                return NativeExpressionType.BINARY_OPERATION;
            } else {
                throw new AssertionError("Wrong number of children for node MINUS: " + childCount);
            }
        }
        throw new AssertionError("Non-expression AST: " + expr);
    }

    private static boolean isLocation(AST unknown) {
        if (unknown.getType() == ID && unknown.getNumberOfChildren() == 0) {
            // It could be a scalar location.
            return true;
        }
        // The only other possibility is ARRAY_LOCATION.
        return unknown.getType() == ARRAY_LOCATION;
    }

    public static NativeLiteral literal(AST nativeLiteral) {
        checkChildCount(0, nativeLiteral);
        int type = nativeLiteral.getType();
        if (type == CHAR) {
            // TODO(jasonpr): Check that this is handled properly. Is the text
            // "a" or "'a'"?
            return new CharLiteral(nativeLiteral.getText());
        } else if (type == TRUE || type == FALSE) {
            return new BooleanLiteral(nativeLiteral.getText());
        } else if (type == INT_LITERAL) {
            return new IntLiteral(nativeLiteral.getText());
        } else {
            throw new AssertionError("Node is not a native literal: " + nativeLiteral);
        }
    }

    public static UnaryOperation unaryOperation(AST unaryOperation) {
        UnaryOperator operator = UNARY_OPERATORS.get(unaryOperation.getType());
        if (operator == null) {
            throw new AssertionError("Non-unary-operation node: " + unaryOperation);
        }
        checkChildCount(1, unaryOperation);
        return new UnaryOperation(operator, nativeExpression(unaryOperation.getFirstChild()));
    }

    public static BinaryOperation binaryOperation(AST binaryOperation) {
        BinaryOperator operator = BINARY_OPERATORS.get(binaryOperation.getType());
        if (operator == null) {
            throw new AssertionError("Non-binary-operation node: " + binaryOperation);
        }
        checkChildCount(2, binaryOperation);
        List<AST> children = children(binaryOperation);
        return new BinaryOperation(operator, nativeExpression(children.get(0)),
                nativeExpression(children.get(1)));
    }

    public static NativeExpression unaryMinus(AST unaryMinus) {
        checkType(unaryMinus, MINUS);
        checkChildCount(1, unaryMinus);
        AST child = unaryMinus.getFirstChild();
        if (child.getType() == INT_LITERAL) {
            checkChildCount(0, child);
            // Let these two nodes (unary minus, and single positive int literal
            // leaf) with one node (single negative int literal leaf).
            // Replace -(128) with -128, for example.
            return new IntLiteral("-" + child.getText());
        } else {
            // No special behavior is needed for anything that is not an int
            // literal. Treat it normally.
            return unaryOperation(unaryMinus);
        }
    }

    public static TernaryOperation ternaryOperation(AST ternaryOperation) {
        checkType(ternaryOperation, QUESTION);
        checkChildCount(3, ternaryOperation);
        List<AST> children = children(ternaryOperation);
        NativeExpression condition = nativeExpression(children.get(0));
        NativeExpression trueResult = nativeExpression(children.get(1));
        NativeExpression falseResult = nativeExpression(children.get(2));
        return new TernaryOperation(condition, trueResult, falseResult);
    }

    public static Location location(AST location) {
        if (location.getType() == ID) {
            return scalarLocation(location);
        } else if (location.getType() == ARRAY_LOCATION) {
            return arrayLocation(location);
        } else {
            throw new AssertionError("AST " + location + " is not a location.");
        }
    }

    public static ScalarLocation scalarLocation(AST scalarLocation) {
        // All sanity checks are performed in stringFromId.
        return new ScalarLocation(stringFromId(scalarLocation));
    }

    public static ArrayLocation arrayLocation(AST arrayLocation) {
        checkType(arrayLocation, ARRAY_LOCATION);
        checkChildCount(2, arrayLocation);
        List<AST> children = children(arrayLocation);
        return new ArrayLocation(stringFromId(children.get(0)), nativeExpression(children.get(1)));
    }

    public static IntLiteral intLiteral(AST intLiteral) {
        checkType(intLiteral, INT_LITERAL);
        checkChildCount(0, intLiteral);
        return new IntLiteral(intLiteral.getText());
    }

    public static List<GeneralExpression> methodCallArgs(AST methodCallArgs) {
        checkType(methodCallArgs, METHOD_CALL_ARGS);
        ImmutableList.Builder<GeneralExpression> builder = ImmutableList.builder();
        for (AST argument : children(methodCallArgs)) {
            builder.add(generalExpression(argument));
        }
        return builder.build();
    }

    /**
     * Make a GeneralExpression from an ANTLR method_call_arg.
     *
     * <p>
     * We would have used the name general_expression in the ANTLR grammar, but
     * general expressions are only used for method_call_arg, so we used the
     * specific name there.
     */
    public static GeneralExpression generalExpression(AST methodCallArg) {
        if (methodCallArg.getType() == STRING) {
            return stringLiteral(methodCallArg);
        } else {
            // Just delegate to the NativeExpression generator. If it's an
            // error, it will be reported there.
            return nativeExpression(methodCallArg);
        }
    }

    public static StringLiteral stringLiteral(AST stringLiteral) {
        checkType(stringLiteral, STRING);
        checkChildCount(0, stringLiteral);
        // TODO(jasonpr): Check that this works as expected.  How are the leading and trailing
        // quotes handled?
        return new StringLiteral(stringLiteral.getText());
    }

    public static BreakStatement breakStatement(AST breakStatement) {
        checkType(breakStatement, BREAK);
        checkChildCount(0, breakStatement);
        return new BreakStatement();
    }

    public static ContinueStatement continueStatement(AST continueStatement) {
        checkType(continueStatement, CONTINUE);
        checkChildCount(0, continueStatement);
        return new ContinueStatement();
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
