package edu.mit.semantics;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
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
                    "Type error in assignment at %s: operation applies to type integer, not %s",
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
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
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
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
    }

    /**
     * Check that the types are all semantically correct in a method call, recursively.
     *
     * @param methodCall The method-call node.
     * @param scope The scope in which the parameters are evaluated.
     * @param errorAccumulator Errors are added to this accumulator.
     */
    private Optional<BaseType> checkMethodCall(MethodCall methodCall, Scope scope,
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
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented!");
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
            checkMethodCall((MethodCall) statement, scope, errorAccumulator);
        } else if (statement instanceof ReturnStatement) {
            checkReturnStatement((ReturnStatement) statement, scope, errorAccumulator);
        } else if (statement instanceof WhileLoop) {
            checkWhileLoop((WhileLoop) statement, scope, errorAccumulator);
        } else {
            throw new AssertionError("Unexpected Statement type for " + statement);
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

    private Optional<BaseType> validNativeExpressionType(NativeExpression expression, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented.");
    }

    private Optional<BaseType> validLocationType(Location location, Scope scope,
            List<SemanticError> errorAccumulator) {
        // TODO(jasonpr): Implement!
        throw new RuntimeException("Not yet implemented.");
    }
}
