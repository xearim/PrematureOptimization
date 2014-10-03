package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.*;
import edu.mit.compilers.ir.EvaluateCheck;
import edu.mit.semantics.errors.SemanticError;
import edu.mit.semantics.errors.UsedBeforeDeclaredSemanticError;

/**
 * Checks for the following rule/s:
 *
 * 2) No identifier is used before it is declared.
 */
public class UsedBeforeDeclaredSemanticCheck implements SemanticCheck {
    List<SemanticError> errors = new ArrayList<SemanticError>();
    private Program prog;

    public UsedBeforeDeclaredSemanticCheck(Program prog) {
        this.prog = prog;
    }

    /**
     * Goes through each method and checks all identifiers to make sure they
     * have been declared.
     */
    @Override
    public List<SemanticError> doCheck() {
        // Go into each method
        @SuppressWarnings("unchecked")
        Iterable<Method> methods = (Iterable<Method>) this.prog.getMethods().getChildren();

        for (Method method: methods) {
            checkBlock(method.getBlock());
        }

        return errors;
    }

    /**
     * Goes through the block's statements and finds its identifiers.
     * Recurses on 'if', 'while', and 'for' loop blocks.
     */
    private void checkBlock(Block block) {
        Scope blockScope = block.getScope();

        for (Statement stmt : block.getStatements()) {
            // Recurse on 'if', 'while', and 'for'
            Iterable<Block> subBlocks = stmt.getBlocks();
            for (Block subBlock: subBlocks){
                checkBlock(subBlock);
            }

            // Check immediate fields
            if (stmt instanceof Assignment){
                checkLocation(((Assignment) stmt).getLocation(), blockScope);
                checkNativeExpression(((Assignment) stmt).getExpression(), blockScope);
            } else if (stmt instanceof BreakStatement) {
                continue;
            } else if (stmt instanceof ContinueStatement) {
                continue;
            } else if (stmt instanceof ForLoop) {
                checkScalarLocation(((ForLoop) stmt).getLoopVariable(), blockScope);
                checkNativeExpression(((ForLoop) stmt).getRangeStart(), blockScope);
                checkNativeExpression(((ForLoop) stmt).getRangeEnd(), blockScope);
            } else if (stmt instanceof IfStatement) {
                checkNativeExpression(((IfStatement) stmt).getCondition(), blockScope);
            } else if (stmt instanceof MethodCall) {
                checkMethodCall((MethodCall) stmt, blockScope);
            } else if (stmt instanceof ReturnStatement) {
                Optional<NativeExpression> rtnVal = ((ReturnStatement) stmt).getValue();
                if (rtnVal.isPresent()){
                    checkNativeExpression(rtnVal.get(), blockScope);
                }
            } else if (stmt instanceof WhileLoop) {
                checkNativeExpression(((WhileLoop) stmt).getCondition(), blockScope);
            } else {
                // TODO(Manny) throw error
            }
        }
    }

    private void checkLocation(Location loc, Scope scope) {
        if (loc instanceof ArrayLocation) {
            checkArrayLocation((ArrayLocation) loc, scope);
        } else if (loc instanceof ScalarLocation) {
            checkScalarLocation((ScalarLocation) loc, scope);
        } else {
            // TODO(Manny) throw error
        }
    }

    /**
     * Recurses through the general expressions in the method call.
     */
    private void checkMethodCall(MethodCall mc, Scope scope) {
        @SuppressWarnings("unchecked")
        Iterable<GeneralExpression> geItr = (Iterable<GeneralExpression>) ((MethodCall) mc).getChildren();
        for (GeneralExpression ge : geItr) {
            checkGeneralExpression(ge, scope);
        }
    }

    /**
     * Distinguishes between StringLiteral and NativeExpressions
     */
    private void checkGeneralExpression(GeneralExpression ge, Scope scope) {
        if (ge instanceof NativeExpression) {
            checkNativeExpression((NativeExpression) ge, scope);
        } else if (ge instanceof StringLiteral) {
            return;
        } else {
            // TODO(Manny) Throw Exception
        }
    }

    /**
     * Checks all possibilities for a native expression.
     */
    private void checkNativeExpression(NativeExpression ne, Scope scope) {
        if (ne instanceof BinaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ne.getChildren();

            for (NativeExpression operand : children) {
                checkNativeExpression(operand, scope);
            }
        } else if (ne instanceof ArrayLocation) {
            checkArrayLocation((ArrayLocation) ne, scope);
        } else if (ne instanceof ScalarLocation) {
            checkScalarLocation((ScalarLocation) ne, scope);
        } else if (ne instanceof MethodCall) {
            checkMethodCall((MethodCall) ne, scope);
        } else if (ne instanceof NativeLiteral) {
            return;
        } else if (ne instanceof TernaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ((TernaryOperation) ne).getChildren();

            for (NativeExpression subNE : children) {
                checkNativeExpression(subNE,scope);
            }
        } else if (ne instanceof UnaryOperation) {
            checkNativeExpression(((UnaryOperation) ne).getArgument(), scope);
        }
    }

    /**
     * Check variable and index
     */
    private void checkArrayLocation(ArrayLocation al, Scope scope) {
        if (!EvaluateCheck.evaluatesTo(al, scope).isPresent()){
            errors.add(new UsedBeforeDeclaredSemanticError(
                    al.getVariableName(), al.getLocationDescriptor()));
        }
        checkNativeExpression(al.getIndex(), scope);
    }

    private void checkScalarLocation(ScalarLocation sl, Scope scope) {
        if (!EvaluateCheck.evaluatesTo(sl, scope).isPresent()) {
            errors.add(new UsedBeforeDeclaredSemanticError(
                    sl.getName(), sl.getLocationDescriptor()));
        }
    }
}
