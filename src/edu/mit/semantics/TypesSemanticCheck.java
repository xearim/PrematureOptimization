package edu.mit.semantics;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.ReturnStatement;
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
            checkBlock(method.getBlock(), errorAccumulator);
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
    private void checkBlock(Block block, List<SemanticError> errorAccumulator) {
        for (Statement statement : block.getStatements()) {
            checkStatement(statement, block.getScope(), errorAccumulator);
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
        Utils.check(expected.equals(actual), errorAccumulator,
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
    private void checkForLoop(ForLoop forLoop, Scope scope, List<SemanticError> errorAccumulator) {
        ScalarLocation loopVariable = forLoop.getLoopVariable();
        Optional<BaseType> loopVariableType = validScalarLocationType(loopVariable, scope,
                errorAccumulator);
        if (loopVariableType.isPresent()) {
            Utils.check(loopVariableType.get().equals(BaseType.INTEGER), errorAccumulator,
                    "Type mismatch for loop variable %s at %s: expected integer but got %s",
                    loopVariable.getVariableName(), loopVariable.getLocationDescriptor(),
                    loopVariableType.get());
        }
        checkBlock(Iterables.getOnlyElement(forLoop.getBlocks()), errorAccumulator);
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
        Utils.check(actualType.get().equals(type), errorAccumulator,
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
            List<SemanticError> errorAccumulator) {
        for (Block block : ifStatement.getBlocks()) {
            checkBlock(block, errorAccumulator);
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
            Utils.check(conditionType.get().equals(BaseType.BOOLEAN), errorAccumulator,
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
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }
    
    /**
     * Check that the types are all semantically correct in a return statement, recursively.
     *
     * @param returnStatement The return-statement node.
     * @param scope The scope in which the parameters are evaluated.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private void checkReturnStatement(ReturnStatement returnStatement, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }

    private void checkWhileLoop(WhileLoop whileLoop, Scope scope,
            List<SemanticError> errorAccumulator) {
        checkCondition(whileLoop.getCondition(), scope, errorAccumulator);
        checkBlock(Iterables.getOnlyElement(whileLoop.getBlocks()), errorAccumulator);
        Optional<IntLiteral> maxRepetitions = whileLoop.getMaxRepetitions();
        if (maxRepetitions.isPresent()) {
            // maxRepetitions must be a positive integer (SR22).
            // TODO(jasonpr): Tie in manny's positive-checking code.
            throw new RuntimeException("Not yet implemented!");
        }

    }

    /** Delegate to the semantic type checkers for various statement types. */
    private void checkStatement(Statement statement, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Use a visitor pattern if these instanceofs get out of
        // hand.  Note that they isn't quite as bad as one might think, because
        // we're very unlikely to add any more implementations of Statement in
        // this project.
        if (statement instanceof Assignment) {
            checkAssignment((Assignment) statement, scope, errorAccumulator);
        } else if (statement instanceof Block) {
            checkBlock((Block) statement, errorAccumulator);
        } else if (statement instanceof BreakStatement) {
            // Breaks have no type-related errors.
            return;
        } else if (statement instanceof ContinueStatement) {
            // Continues have no type-related errors.
            return;
        } else if (statement instanceof ForLoop) {
            checkForLoop((ForLoop) statement, scope, errorAccumulator);
        } else if (statement instanceof IfStatement) {
            checkIfStatement((IfStatement) statement, scope, errorAccumulator);
        } else if (statement instanceof MethodCall) {
            // If it's just in the list of statements, we don't care about its
            // type. We discard it.
            validMethodCallType((MethodCall) statement, scope, errorAccumulator);
        } else if (statement instanceof ReturnStatement) {
            checkReturnStatement((ReturnStatement) statement, scope, errorAccumulator);
        } else if (statement instanceof WhileLoop) {
            checkWhileLoop((WhileLoop) statement, scope, errorAccumulator);
        } else {
            throw new AssertionError("Unexpected Statement type for " + statement);
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
                for (NativeExpression expression : operation.getChildren()) {
                    checkTypedExpression(BaseType.BOOLEAN, expression, scope, errorAccumulator);
                }
                return Optional.of(BaseType.BOOLEAN);
            case PLUS:
            case MINUS:
            case DIVIDED_BY:
            case TIMES:
            case MODULO:
                for (NativeExpression expression : operation.getChildren()) {
                    checkTypedExpression(BaseType.INTEGER, expression, scope, errorAccumulator);
                }
                return Optional.of(BaseType.INTEGER);
            case DOUBLE_EQUALS:
            case NOT_EQUALS:
                Optional<BaseType> leftType = validNativeExpressionType(
                        operation.getLeftArgument(), scope, errorAccumulator);
                Optional<BaseType> rightType = validNativeExpressionType(
                        operation.getRightArgument(), scope, errorAccumulator);
                if (allPresent(leftType, rightType)) {
                    Utils.check(leftType.equals(rightType), errorAccumulator,
                            "Type mismatch at %s: expected equal types, but got %s and %s.",
                            operation.getLocationDescriptor(), leftType.get(), rightType.get());
                    // No need to check rightType: it's equal to leftType.
                    Utils.check(
                            leftType.get().equals(BaseType.BOOLEAN)
                                    || leftType.get().equals(BaseType.INTEGER), errorAccumulator,
                            "Type mismatch at %s: expected an integer or a boolean, but got %s",
                            operation.getLocationDescriptor(), leftType.get());
                }
                return Optional.of(BaseType.BOOLEAN);
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
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
            TernaryOperation opeartion, Scope scope, List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validUnaryOperationType(
            UnaryOperation operation, Scope scope, List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validNativeLiteralType(
            NativeLiteral literal, Scope scope, List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validLocationType(Location location, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented.");
    }

    /** See validNativeExpressionType. */
    private Optional<BaseType> validScalarLocationType(ScalarLocation location, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented.");
    }

    private static boolean allPresent(Optional<?>... optionals) {
        for (Optional<?> optional : optionals) {
            if (!optional.isPresent()) {
                return false;
            }
        }
        return true;
    }
}
