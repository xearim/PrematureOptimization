package edu.mit.semantics;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.semantics.NonPositiveArrayLengthSemanticCheck.isNonPositiveIntLiteral;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BooleanLiteral;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.Callout;
import edu.mit.compilers.ast.CharLiteral;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.ReturnType;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.semantics.errors.SemanticError;

public class TypesSemanticCheck implements SemanticCheck {

    private final Program program;

    public TypesSemanticCheck(Program program) {
        this.program = program;
    }

    @Override
    public List<SemanticError> doCheck() {
        return doCheck(program);
    }
    
    // Eventually, I think we'll want doCheck to take a Program parameter.
    // Writing the method this way, now, to ease the transition, should we
    // decide to make it.
    public List<SemanticError> doCheck(Program program) {
        List<SemanticError> errorAccumulator = new ArrayList<SemanticError>();
        // Only blocks hold type-checkable elements. All blocks live under a
        // method.
        for (Method method : program.getMethods()) {
            ReturnType returnType = method.getReturnType();
            checkState(returnType.getReturnType().isPresent());
            checkBlock(method.getBlock(), errorAccumulator,
                    method.getReturnType().getReturnType().get());
        }
        return errorAccumulator;
    }

    /**
     * Check that the types are all semantically correct in a block, recursively.
     * 
     * <p>Errors in this block are added to 'errorAccumulator'.
     * 
     * <p>This takes no scope: The block knows its own scope, and the scopes of its
     *    parents. 
     */
    private void checkBlock(Block block, List<SemanticError> errorAccumulator, BaseType returnType) {
        for (Statement statement : block.getStatements()) {
            checkStatement(statement, block.getScope(), errorAccumulator, returnType);
        }
    }

    /**
     * Check that the types are all semantically correct in an assignment, recursively.
     *
     * <p>This check corresponds to SR19 and SR20.
     *
     * @param assignment The assignment node.
     * @param scope The scope in which the assignment is performed.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private void checkAssignment(Assignment assignment, Scope scope,
            List<SemanticError> errorAccumulator) {
        Location destination = assignment.getLocation();
        NativeExpression expression = assignment.getExpression();

        Optional<BaseType> expected = validLocationType(destination, scope, errorAccumulator);
        Optional<BaseType> actual = validNativeExpressionType(expression, scope, errorAccumulator);

        if (!allPresent(expected, actual)) {
            // Children had errors, so we don't try to use their results.
            return;
        }

        // The parser should have guaranteed this.
        checkState(expected.get() != BaseType.VOID);

        // Increments/decrements must operate over ints (SR20).
        AssignmentOperation operation = assignment.getOperation();
        if (operation == AssignmentOperation.MINUS_EQUALS
                || operation == AssignmentOperation.PLUS_EQUALS) {
            Utils.check(expected.equals(BaseType.INTEGER), errorAccumulator,
                    "Type error in assignment at %s: operation applies to type integer, not %s.",
                    assignment.getLocationDescriptor(), expected.get());
        }

        // All assignments must have the same type on each side (SR19).
        Utils.check(actual.get().isA(expected.get()), errorAccumulator,
                "Type mismatch in assignment at %s: expected %s but got %s.",
                assignment.getLocationDescriptor(), expected.get(), actual.get());
    }

    /**
     * Check that the types are all semantically correct in an assignment, recursively.
     *
     * @param forLoop The for-loop node.
     * @param scope The scope in which the loop's setup/control takes place.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private void checkForLoop(ForLoop forLoop, Scope scope, List<SemanticError> errorAccumulator,
            BaseType returnType) {
        ScalarLocation loopVariable = forLoop.getLoopVariable();
        Optional<BaseType> loopVariableType = validScalarLocationType(loopVariable, scope,
                errorAccumulator);
        if (loopVariableType.isPresent()) {
            Utils.check(loopVariableType.get().equals(BaseType.INTEGER), errorAccumulator,
                    "Type mismatch for loop variable %s at %s: expected integer but got %s",
                    loopVariable.getVariableName(), loopVariable.getLocationDescriptor(),
                    loopVariableType.get());
        }
        checkBlock(Iterables.getOnlyElement(forLoop.getBlocks()), errorAccumulator, returnType);
        // For loops must have integer bounds (SR21).
        checkTypedExpression(BaseType.INTEGER, forLoop.getRangeStart(), scope, errorAccumulator);
        checkTypedExpression(BaseType.INTEGER, forLoop.getRangeEnd(), scope, errorAccumulator);
    }

    private void checkTypedExpression(BaseType type, NativeExpression expression, Scope scope,
            List<SemanticError> errorAccumulator) {
        Optional<BaseType> actualType = validNativeExpressionType(expression, scope,
                errorAccumulator);
        if (!actualType.isPresent()) {
            // There was an error below that makes it impossible to check
            // whether its type.
            // Give up on this check.
            return;
        }
        Utils.check(actualType.get().isA(type), errorAccumulator,
                "Type mismatch at %s: expected %s but got %s.", expression.getLocationDescriptor(),
                type, actualType.get());
    }


    /**
     * Check that the types are all semantically correct in an if statement, recursively.
     *
     * @param ifStatement The if-statement node.
     * @param scope The scope in which the loop's condition evalutates.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private void checkIfStatement(IfStatement ifStatement, Scope scope,
            List<SemanticError> errorAccumulator, BaseType returnType) {
        for (Block block : ifStatement.getBlocks()) {
            checkBlock(block, errorAccumulator, returnType);
        }

        checkCondition(ifStatement.getCondition(), scope, errorAccumulator);
    }

    /**
     * Check that the type of a condition is a boolean.
     *
     * <p>Additionally, recursively check the children of the condition expression.
     */
    private void checkCondition(NativeExpression condition, Scope scope,
            List<SemanticError> errorAccumulator) {
        // No matter the result of the block checks, we can still check the
        // condition.
        Optional<BaseType> conditionType =
                validNativeExpressionType(condition, scope, errorAccumulator);

        // Condition statements must have boolean conditions (SR13).
        if (conditionType.isPresent()) {
            Utils.check(conditionType.get().isA(BaseType.BOOLEAN), errorAccumulator,
                    "Type mismatch in condition at %s: expected boolean but got $s.",
                    condition.getLocationDescriptor(), conditionType.get());
        }
    }

    /**
     * Check that the types are all semantically correct in a method call, recursively.
     *
     * @param methodCall The method-call node.
     * @param scope The scope in which the parameters are evaluated.
     * @param errorAccumulator Errors are added to this accumulator.
     * @return The type that this method call returns.
     */
    private Optional<BaseType> validMethodCallType(MethodCall methodCall, Scope scope,
            List<SemanticError> errorAccumulator) {

        String methodName = methodCall.getMethodName();
        Optional<Method> calledMethod = lookupMethodWithName(methodName);
        boolean isMethod = calledMethod.isPresent();

        if (!isMethod && !isCallout(methodName)) {
            Utils.check(false, errorAccumulator,
                    "Failed lookup: Could not find method or callout %s at %s", methodName,
                    methodCall.getLocationDescriptor());
            return Optional.absent();
        }

        int parameterNumber = 0;
        for (GeneralExpression expression : methodCall.getParameterValues()) {
            if (expression instanceof NativeExpression) {
                NativeExpression nativeExpression = (NativeExpression) expression;
                if (isMethod) {
                    checkTypedExpression(calledMethod.get().getSignature().get(parameterNumber),
                            nativeExpression, scope, errorAccumulator);
                } else {
                    // Just make sure it evaluates.
                    validNativeExpressionType(nativeExpression, scope, errorAccumulator);
                }
            }
            // Otherwise, the expression is non-native.
            // If this is a method, the SR7 check takes care of this
            // case, so we don't have to.
            // If it's a callout, we don't need to check it, because non-native
            // expressions don't fit into the type system.
            // So, for non-native expressions, we do nothing!
            parameterNumber++;
        }

        // TODO(jasonpr): Remove this global-ish reference, if possible?
        if (isMethod) {
            ReturnType type = calledMethod.get().getReturnType();
            // TODO(jasonpr): Remove the Optional, which was a holdover from the time when
            // void was the absense of a type.  (Or, hold off on that.  That time may come back.)
            checkState(type.getReturnType().isPresent());
            // The evaluation type of a method is its return type (stronger
            // version of SR06).
            return type.getReturnType();
        } else {
            // It's a callout.
            return Optional.of(BaseType.WILDCARD);
        }
    }
    
    /**
     * Check that the types are all semantically correct in a return statement, recursively.
     *
     * @param returnStatement The return-statement node.
     * @param scope The scope in which the parameters are evaluated.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private void checkReturnStatement(ReturnStatement returnStatement, Scope scope,
            List<SemanticError> errorAccumulator, BaseType returnType) {
        Optional<NativeExpression> value = returnStatement.getValue();
        if (returnType == BaseType.VOID) {
            // There should be no return value at all (SR8).
            Utils.check(!value.isPresent(), errorAccumulator,
                    "Improper return at %s: return argument in a void method.",
                    returnStatement.getLocationDescriptor());
        } else {
            if (value.isPresent()) {
                // The return value should match (SR9).
                checkTypedExpression(returnType, value.get(), scope, errorAccumulator);
            } else {
                // There must be a returned value
                Utils.check(
                        value.isPresent(),
                        errorAccumulator,
                        "Improper return at %s: no value was proveded, but an expression of type %s was required.",
                        returnStatement.getLocationDescriptor(), returnType);
            }
        }
    }

    private void checkWhileLoop(WhileLoop whileLoop, Scope scope,
            List<SemanticError> errorAccumulator, BaseType returnType) {
        checkCondition(whileLoop.getCondition(), scope, errorAccumulator);
        checkBlock(Iterables.getOnlyElement(whileLoop.getBlocks()), errorAccumulator, returnType);
        Optional<IntLiteral> maxRepetitions = whileLoop.getMaxRepetitions();
        if (maxRepetitions.isPresent()) {
            // maxRepetitions must be a positive integer (SR22).
            Utils.check(isNonPositiveIntLiteral(maxRepetitions.get().getName()),
                    errorAccumulator,
                    "Invalid upper bound for while loop at %s: expected a positive integer, but got %s",
                    maxRepetitions.get().getLocationDescriptor(), maxRepetitions.get().getName());
            
            throw new RuntimeException("Not yet implemented!");
        }

    }

    /** Delegate to the semantic type checkers for various statement types. */
    private void checkStatement(Statement statement, Scope scope,
            List<SemanticError> errorAccumulator, BaseType returnType) {
        // TODO(jasonpr): Use a visitor pattern if these instanceofs get out of
        // hand.  Note that they isn't quite as bad as one might think, because
        // we're very unlikely to add any more implementations of Statement in
        // this project.
        if (statement instanceof Assignment) {
            checkAssignment((Assignment) statement, scope, errorAccumulator);
        } else if (statement instanceof Block) {
            checkBlock((Block) statement, errorAccumulator, returnType);
        } else if (statement instanceof BreakStatement) {
            // Breaks have no type-related errors.
            return;
        } else if (statement instanceof ContinueStatement) {
            // Continues have no type-related errors.
            return;
        } else if (statement instanceof ForLoop) {
            checkForLoop((ForLoop) statement, scope, errorAccumulator, returnType);
        } else if (statement instanceof IfStatement) {
            checkIfStatement((IfStatement) statement, scope, errorAccumulator, returnType);
        } else if (statement instanceof MethodCall) {
            // If it's just in the list of statements, we don't care about its
            // type. We discard it.
            validMethodCallType((MethodCall) statement, scope, errorAccumulator);
        } else if (statement instanceof ReturnStatement) {
            checkReturnStatement((ReturnStatement) statement, scope, errorAccumulator, returnType);
        } else if (statement instanceof WhileLoop) {
            checkWhileLoop((WhileLoop) statement, scope, errorAccumulator, returnType);
        } else {
            throw new AssertionError("Unexpected Statement type for " + statement);
        }
    }

    // TODO(jasonpr): Modify the grammar so that array names are not interpreted
    // as locations.
    /**
     * Assert that this Location actually points to an array variable.
     */
    private void checkArray(ScalarLocation location, Scope scope,
            List<SemanticError> errorAccumulator) {
        // Reports an error if the name is not found.
        Optional<FieldDescriptor> descriptor = lookup(location, scope, errorAccumulator);
        if (descriptor.isPresent()) {
            // Make sure it's an array.
            Utils.check(descriptor.get().getLength().isPresent(), errorAccumulator,
                    "Type mismatch for %s at %s: expected an array, but got ",
                    location.getVariableName(), location.getLocationDescriptor());
        }
    }


    /**
     * Get the BaseType to which the expression evaluates.  At the same time, check this
     * expression and its children for semantic errors.
     *
     * <p>If it is impossible to decide, of semantic errors, then return Optional.absent(). But,
     * if it is still possible to infer what the type must be if there were no semantic errors,
     * then return that type
     *
     * @param scope The scope in which the expression is evaluated.
     * @param errorAccumulator Errors are added to this accumulator.
     * @return The type of this expression.  If it is impossible to decide, due to to semantic
     * errors, then return Optional.absent().  But, if it is possible to infer what the type must
     * have been if there were no semantic errors, then return that type.
     */
    private Optional<BaseType> validNativeExpressionType(NativeExpression expression, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Use a visitor pattern if these instanceofsget out of
        // hand.
        // But, as in checkStatement, the current condition isn't so dire.
        if (expression instanceof BinaryOperation) {
            return validBinaryOperationType((BinaryOperation) expression, scope, errorAccumulator);
        } else if (expression instanceof MethodCall) {
            return validMethodCallType((MethodCall) expression, scope, errorAccumulator);
        } else if (expression instanceof TernaryOperation) {
            return validTernaryOperationType((TernaryOperation) expression, scope, errorAccumulator);
        } else if (expression instanceof UnaryOperation) {
            return validUnaryOperationType((UnaryOperation) expression, scope, errorAccumulator);
        } else if (expression instanceof Location) {
            return validLocationType((Location) expression, scope, errorAccumulator);
        } else if (expression instanceof NativeLiteral) {
            return validNativeLiteralType((NativeLiteral) expression, scope, errorAccumulator);
        } else {
            throw new AssertionError("Unexpected NativeExpression type for " + expression);
        }
    }
    
    /** See validNativeExpressionType. */
    private Optional<BaseType> validBinaryOperationType(
            BinaryOperation operation, Scope scope, List<SemanticError> errorAccumulator) {
        switch (operation.getOperator()) {
            case AND:
            case OR:
                // Boolean binary operations must take booleans (binary part of
                // SR18).
                for (NativeExpression expression : operation.getChildren()) {
                    checkTypedExpression(BaseType.BOOLEAN, expression, scope, errorAccumulator);
                }
                return Optional.of(BaseType.BOOLEAN);
            case PLUS:
            case MINUS:
            case DIVIDED_BY:
            case TIMES:
            case MODULO:
                // Arithmetic binary operations must take integers (half of
                // SR16).
                for (NativeExpression expression : operation.getChildren()) {
                    checkTypedExpression(BaseType.INTEGER, expression, scope, errorAccumulator);
                }
                return Optional.of(BaseType.INTEGER);
            case DOUBLE_EQUALS:
            case NOT_EQUALS:
                // Equality operators must take two ints or two booleans (SR17).
                Optional<BaseType> leftType = validNativeExpressionType(
                        operation.getLeftArgument(), scope, errorAccumulator);
                Optional<BaseType> rightType = validNativeExpressionType(
                        operation.getRightArgument(), scope, errorAccumulator);
                if (allPresent(leftType, rightType)) {
                    Utils.check(
                            bothAre(leftType.get(), rightType.get(), BaseType.BOOLEAN,
                                    BaseType.INTEGER),
                            errorAccumulator,
                            "Type mismatch at %s: expected equal non-void types, but got %s and %s.",
                            operation.getLocationDescriptor(), leftType.get(), rightType.get());
                }
                return Optional.of(BaseType.BOOLEAN);
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
                // Relative binary operations must take integers (half of SR16).
                for (NativeExpression expression : operation.getChildren()) {
                    checkTypedExpression(BaseType.INTEGER, expression, scope, errorAccumulator);
                }
                return Optional.of(BaseType.BOOLEAN);
            default:
                throw new AssertionError("Got unexpected operator " + operation.getOperator());
        }
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validTernaryOperationType(
            TernaryOperation operation, Scope scope, List<SemanticError> errorAccumulator) {
        // Check that the ternary condition is, in fact, a condition (SR14).
        checkCondition(operation.getCondition(), scope, errorAccumulator);
        Optional<BaseType> trueResultType = validNativeExpressionType(
                operation.getTrueResult(), scope, errorAccumulator);
        Optional<BaseType> falseResultType = validNativeExpressionType(
                operation.getFalseResult(), scope, errorAccumulator);

        if (!allPresent(trueResultType, falseResultType)) {
            // We can't tell what it returns!
            return Optional.absent();
        }

        // Check that the two expressions have the same type (SR15).
        if (!bothAre(trueResultType.get(), falseResultType.get(), BaseType.BOOLEAN,
                BaseType.INTEGER)) {
            Utils.check(false, errorAccumulator,
                    "Type mismatch at %s: Expected same types, but got %s and %s.",
                    operation.getTrueResult().getLocationDescriptor(),
                    trueResultType.get(), falseResultType.get());
            return Optional.absent();
        }
        return trueResultType;
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validUnaryOperationType(
            UnaryOperation operation, Scope scope, List<SemanticError> errorAccumulator) {
        NativeExpression argument = operation.getArgument();
        switch (operation.getOperator()) {
            case ARRAY_LENGTH:
                // This is true, but very strange.
                // TODO(jasonpr): Modify grammar so this isn't the case.
                checkState(argument instanceof ScalarLocation);

                // The argument of the "@" operator must be an array (SR12).
                // We don't actually care about its type, or whether it
                // succeeded! Discard the result.
                checkArray((ScalarLocation) operation.getArgument(), scope, errorAccumulator);

                return Optional.of(BaseType.INTEGER);
            case NEGATIVE:
                checkTypedExpression(
                        BaseType.INTEGER, operation.getArgument(), scope, errorAccumulator);
                return Optional.of(BaseType.INTEGER);
            case NOT:
                // Logical not must have a boolean argument (half of SR18).
                checkTypedExpression(
                        BaseType.BOOLEAN, operation.getArgument(), scope, errorAccumulator);
                return Optional.of(BaseType.BOOLEAN);
            default:
                throw new AssertionError("Got unexpected operator: " + operation.getOperator());
        }
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validNativeLiteralType(
            NativeLiteral literal, Scope scope, List<SemanticError> errorAccumulator) {
        if (literal instanceof BooleanLiteral) {
            return Optional.of(BaseType.BOOLEAN);
        } else if (literal instanceof IntLiteral) {
            return Optional.of(BaseType.INTEGER);
        } else if (literal instanceof CharLiteral) {
            // TODO(jasonpr): Make parser reject misplaced char literals, so it doesn't need to
            // extend NativeLiteral... it's not really native, after all!
            Utils.check(false, errorAccumulator,
                    "Type mismatch at %s: expected an integer or a boolean, but got a char, %s",
                    literal.getLocationDescriptor(), literal.getName());
            return Optional.absent();
        } else {
            throw new AssertionError("Unexpected NativeLiteral " + literal);
        }
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validLocationType(Location location, Scope scope,
            List<SemanticError> errorAccumulator) {
        if (location instanceof ArrayLocation) {
            return validArrayLocationType((ArrayLocation) location, scope, errorAccumulator);
        } else if (location instanceof ScalarLocation) {
            return validScalarLocationType((ScalarLocation) location, scope, errorAccumulator);
        } else {
            throw new AssertionError("Unexpected Location type for " + location);
        }
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validArrayLocationType(ArrayLocation location, Scope scope,
            List<SemanticError> errorAccumulator) {
        // All array locations must have an integer index (second half of SR11).
        checkTypedExpression(BaseType.INTEGER, location.getIndex(), scope, errorAccumulator);

        // All array location must be for an array variable (first half of
        // SR11).
        Optional<FieldDescriptor> descriptor = lookup(location, scope, errorAccumulator);
        if (!descriptor.isPresent()) {
            return Optional.absent();
        }
        if (!descriptor.get().getLength().isPresent()) {
            Utils.check(false, errorAccumulator,
                    "Type mismatch: expected an array location for varialbe %s at %s, but got a scalar.",
                    location.getName(), location.getLocationDescriptor());
            return Optional.absent();
        }
        return Optional.of(descriptor.get().getType());
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validScalarLocationType(ScalarLocation location, Scope scope,
            List<SemanticError> errorAccumulator) {
        Optional<FieldDescriptor> descriptor = lookup(location, scope, errorAccumulator);
        if (!descriptor.isPresent()) {
            return Optional.absent();
        }
        if (descriptor.get().getLength().isPresent()) {
            Utils.check(false, errorAccumulator,
                    "Type mismatch: expected a scalar location for variable %s at %s, but got an array.",
                    location.getVariableName(), location.getLocationDescriptor());
            return Optional.absent();
        }
        return Optional.of(descriptor.get().getType());
    }

    /** Get the descriptor for a variable, and log an error if it's not found. */
    private Optional<FieldDescriptor> lookup(Location location, Scope scope,
            List<SemanticError> errorAccumulator) {
        Optional<FieldDescriptor> descriptor = scope.getFromScope(location.getVariableName());
        if (descriptor.isPresent()) {
            return descriptor;
        } else {
            Utils.check(false, errorAccumulator,
                    "Failed lookup: could not find variable named %s at %s.",
                    location.getVariableName(), location.getLocationDescriptor());
            return Optional.absent();
        }

    }

    private static boolean allPresent(Optional<?>... optionals) {
        for (Optional<?> optional : optionals) {
            if (!optional.isPresent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lookup the method in the global methods table.
     *
     * <p>Do NOT report an error if it's not found.
     *
     * <p>Do NOT look through the callouts table.
     */
    private Optional<Method> lookupMethodWithName(String name) {
        for (Method method : program.getMethods()) {
            if (method.getName().equals(name)) {
                return Optional.of(method);
            }
        }
        return Optional.absent();
    }
    
    private boolean isCallout(String name) {
        for (Callout callout : program.getCallouts()) {
            if (callout.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /** Return whether there's some option such that one.isA(option) and other.isA(option), too. */
    private boolean bothAre(BaseType one, BaseType other, BaseType... options) {
        for (BaseType option : options) {
            if (one.isA(option) && other.isA(option)) {
                return true;
            }
        }
        return false;
    }

}
