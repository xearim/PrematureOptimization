package edu.mit.compilers.semantics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.IfStatement;
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
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.ir.EvaluateCheck;
import edu.mit.compilers.semantics.errors.SemanticError;
import edu.mit.compilers.semantics.errors.UsedBeforeDeclaredSemanticError;

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
            checkBlock(method.getBlock(), method);
        }

        return errors;
    }

    /**
     * Goes through the block's statements and finds its identifiers.
     * Recurses on 'if', 'while', and 'for' loop blocks.
     */
    private void checkBlock(Block block, Method method) {
        Scope blockScope = block.getScope();

        for (Statement stmt : block.getStatements()) {
            // Recurse on 'if', 'while', and 'for'
            Iterable<Block> subBlocks = stmt.getBlocks();
            for (Block subBlock: subBlocks){
                checkBlock(subBlock, method);
            }

            // Check immediate fields
            if (stmt instanceof Assignment){
                checkLocation(((Assignment) stmt).getLocation(), blockScope, method);
                checkNativeExpression(((Assignment) stmt).getExpression(), blockScope, method);
            } else if (stmt instanceof BreakStatement) {
                continue;
            } else if (stmt instanceof ContinueStatement) {
                continue;
            } else if (stmt instanceof ForLoop) {
                checkScalarLocation(((ForLoop) stmt).getLoopVariable(), blockScope);
                checkNativeExpression(((ForLoop) stmt).getRangeStart(), blockScope, method);
                checkNativeExpression(((ForLoop) stmt).getRangeEnd(), blockScope, method);
            } else if (stmt instanceof IfStatement) {
                checkNativeExpression(((IfStatement) stmt).getCondition(), blockScope, method);
            } else if (stmt instanceof MethodCall) {
                checkMethodCall((MethodCall) stmt, blockScope, method);
            } else if (stmt instanceof ReturnStatement) {
                Optional<NativeExpression> rtnVal = ((ReturnStatement) stmt).getValue();
                if (rtnVal.isPresent()){
                    checkNativeExpression(rtnVal.get(), blockScope, method);
                }
            } else if (stmt instanceof WhileLoop) {
                checkNativeExpression(((WhileLoop) stmt).getCondition(), blockScope, method);
            } else {
                // TODO(Manny) throw error
            }
        }
    }

    private void checkLocation(Location loc, Scope scope, Method method) {
        if (loc instanceof ArrayLocation) {
            checkArrayLocation((ArrayLocation) loc, scope, method);
        } else if (loc instanceof ScalarLocation) {
            checkScalarLocation((ScalarLocation) loc, scope);
        } else {
            // TODO(Manny) throw error
        }
    }

    /**
     * Recurses through the general expressions in the method call.
     */
    private void checkMethodCall(MethodCall mc, Scope scope, Method method) {
    	checkMethod(mc, method);
        Iterable<GeneralExpression> geItr = mc.getParameterValues();
        for (GeneralExpression ge : geItr) {
            checkGeneralExpression(ge, scope, method);
        }
    }
    
    private void checkMethod(MethodCall mc, Method method) {
    	for(Method methodCalled : prog.getMethods().getChildren()){
    		if(methodCalled.getName().equals(mc.getMethodName()) &&
    		   prog.getMethods().getSequence().lastIndexOf(methodCalled) > prog.getMethods().getSequence().lastIndexOf(method)){
    				errors.add(new UsedBeforeDeclaredSemanticError(
                        mc.getName(), mc.getLocationDescriptor()));
    		}
    	}
    }

    /**
     * Distinguishes between StringLiteral and NativeExpressions
     */
    private void checkGeneralExpression(GeneralExpression ge, Scope scope, Method method) {
        if (ge instanceof NativeExpression) {
            checkNativeExpression((NativeExpression) ge, scope, method);
        } else if (ge instanceof StringLiteral) {
            return;
        } else {
            // TODO(Manny) Throw Exception
        }
    }

    /**
     * Checks all possibilities for a native expression.
     */
    private void checkNativeExpression(NativeExpression ne, Scope scope, Method method) {
        if (ne instanceof BinaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ne.getChildren();

            for (NativeExpression operand : children) {
                checkNativeExpression(operand, scope, method);
            }
        } else if (ne instanceof ArrayLocation) {
            checkArrayLocation((ArrayLocation) ne, scope, method);
        } else if (ne instanceof ScalarLocation) {
            checkScalarLocation((ScalarLocation) ne, scope);
        } else if (ne instanceof MethodCall) {
            checkMethodCall((MethodCall) ne, scope, method);
        } else if (ne instanceof NativeLiteral) {
            return;
        } else if (ne instanceof TernaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ((TernaryOperation) ne).getChildren();

            for (NativeExpression subNE : children) {
                checkNativeExpression(subNE,scope, method);
            }
        } else if (ne instanceof UnaryOperation) {
            checkNativeExpression(((UnaryOperation) ne).getArgument(), scope, method);
        }
    }

    /**
     * Check variable and index
     */
    private void checkArrayLocation(ArrayLocation al, Scope scope, Method method) {
        if (!EvaluateCheck.evaluatesTo(al, scope).isPresent()){
            errors.add(new UsedBeforeDeclaredSemanticError(
                    al.getVariable().generateName(), al.getLocationDescriptor()));
        }
        checkNativeExpression(al.getIndex(), scope, method);
    }

    private void checkScalarLocation(ScalarLocation sl, Scope scope) {
        if (!EvaluateCheck.evaluatesTo(sl, scope).isPresent()) {
            errors.add(new UsedBeforeDeclaredSemanticError(
                    sl.getName(), sl.getLocationDescriptor()));
        }
    }
}
