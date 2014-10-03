package edu.mit.semantics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.*;
import edu.mit.compilers.ir.EvaluateCheck;
import edu.mit.semantics.errors.SemanticError;
import edu.mit.semantics.errors.SignatureMismatchSemanticError;

public class SignatureMismatchSemanticCheck implements SemanticCheck {
    Program prog;
    List<SemanticError> errors = new ArrayList<SemanticError>();

    public SignatureMismatchSemanticCheck(Program prog) {
        this.prog = prog;
    }

    /**
     * Check MethodCall instances for (MethodName,Signature) matches
     */
    @Override
    public List<SemanticError> doCheck() {
        @SuppressWarnings("unchecked")
        Iterable<Method> methods = (Iterable<Method>) this.prog.getMethods().getChildren();

        // Check each method for method call statements
        for (Method method : methods) {
            checkMethod(method);
        }

        return errors;
    }

    private void checkMethod(Method method) {
        checkBlock(method.getBlock());
    }

    private void checkBlock(Block block) {
        Iterable<Statement> statements = block.getStatements();

        for (Statement statement : statements) {
            // Check for MethodCalls
            if (statement instanceof MethodCall) {
                checkMethodCall((MethodCall) statement, block.getScope());
            } else {
                // Recurse on if, for, while
                Iterable<Block> subBlocks = statement.getBlocks();

                for (Block subBlock : subBlocks) {
                    checkBlock(subBlock);
                }
            }
        }
    }

    private void checkMethodCall(MethodCall mc, Scope scope) {
        // Check name and signature size for quick comparison 
        String name = mc.getName();
        int len;

        // If there is a callout with the same name, then return
        @SuppressWarnings("unchecked")
        Iterable<Callout> callouts = (Iterable<Callout>) this.prog.getCallouts().getChildren();
        for (Callout callout : callouts) {
            if (callout.getName().equals(name)) {
                return;
            }
        }

        // Check if in methods
        len = mc.getParameterValues().getSequence().size();
        @SuppressWarnings("unchecked")
        Iterable<Method> methods = (Iterable<Method>) this.prog.getMethods().getChildren();
        Optional<Method> correspondingMethod = Optional.absent();
        for (Method method : methods) {
            if (name.equals(method.getName())) { // name should only appear once in method symbol table
                correspondingMethod = Optional.of(method);
                if (len == method.getParameters().getVariables().size() &&
                        checkParameterTypes(mc, scope, method)) {
                    return;
                }
                else {
                    break;
                }
            }
        }
        errors.add(new SignatureMismatchSemanticError(this.prog.getName(), mc,
                correspondingMethod));
    }

    private boolean checkParameterTypes(MethodCall mc, Scope mcScope,  Method method) {
        // MethodCall signature
        @SuppressWarnings("unchecked")
        Iterable<GeneralExpression> methodCallSignature = (Iterable<GeneralExpression>) mc.getParameterValues().getChildren();
        // Method signature
        Iterator<BaseType> methodSignature = method.getParameters().getSignature().iterator();

        for (GeneralExpression ge : methodCallSignature) {
            if (ge instanceof StringLiteral) {
                return false;
            } else if (ge instanceof NativeExpression) {
                Optional<BaseType> type = getNativeExpressionType((NativeExpression) ge, mcScope);
                if (!(type.isPresent() && type.get() == methodSignature.next())) {
                    return false;
                }
            } else {
                // TODO(Manny) throw exception
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Optional<BaseType> getNativeExpressionType(NativeExpression ne, Scope scope) {
        if (ne instanceof BinaryOperation) {
            return EvaluateCheck.evaluatesTo((BinaryOperation) ne, scope);
        } else if (ne instanceof ArrayLocation) {
            return EvaluateCheck.evaluatesTo((ArrayLocation) ne, scope);
        } else if (ne instanceof ScalarLocation) {
            return EvaluateCheck.evaluatesTo((ScalarLocation) ne, scope);
        } else if (ne instanceof MethodCall) {
            return EvaluateCheck.evaluatesTo((MethodCall) ne, (Iterable<Method>) this.prog.getMethods().getChildren());
        } else if (ne instanceof BooleanLiteral) {
            return EvaluateCheck.evaluatesTo((BooleanLiteral) ne, scope);
        } else if (ne instanceof CharLiteral) {
            return EvaluateCheck.evaluatesTo((CharLiteral) ne, scope); 
        } else if (ne instanceof IntLiteral) {
            return EvaluateCheck.evaluatesTo((IntLiteral) ne, scope);
        } else if (ne instanceof TernaryOperation) {
            return EvaluateCheck.evaluatesTo((TernaryOperation) ne, scope);
        } else if (ne instanceof UnaryOperation) {
            return EvaluateCheck.evaluatesTo((UnaryOperation) ne, scope);
        } else {
            // TODO(Manny): throw exception
            return Optional.absent();
        }
    }
}
