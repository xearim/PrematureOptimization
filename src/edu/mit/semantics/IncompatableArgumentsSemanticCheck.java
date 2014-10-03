package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.Callout;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.IfStatement;
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
import edu.mit.semantics.errors.IncompatableArgumentsSemanticError;
import edu.mit.semantics.errors.SemanticError;

public class IncompatableArgumentsSemanticCheck implements SemanticCheck{

	Program prog;
    List<SemanticError> errors = new ArrayList<SemanticError>();
    
    public IncompatableArgumentsSemanticCheck(Program prog) {
        this.prog = prog;
    }
    
	@Override
	public List<SemanticError> doCheck() {
		Iterable<Method> methods = this.prog.getMethods().getChildren();

        for (Method method: methods) {
            checkBlock(method.getBlock());
        }
        
        return errors;
	}
	
	private void checkBlock(Block block) {

		// recurse down blocks
        for (Statement stmt : block.getStatements()) {
        	Iterable<Block> subBlocks = stmt.getBlocks();
            for (Block subBlock: subBlocks){
                checkBlock(subBlock);
            }
            
            if (stmt instanceof Assignment){
                continue;
            } else if (stmt instanceof BreakStatement) {
            	continue;
            } else if (stmt instanceof ContinueStatement) {
            	continue;
            } else if (stmt instanceof ForLoop) {
            	continue;
            } else if (stmt instanceof IfStatement) {
            	continue;
            } else if (stmt instanceof MethodCall) {
            	checkMethodCall((MethodCall) stmt, block.getScope());
            } else if (stmt instanceof ReturnStatement) {
                continue;
            } else if (stmt instanceof WhileLoop) {
            	continue;
            }
        }
    }

	// Skip doing anything but recursing on anything but Scalars, where you check for 
	// Arrays being used as inputs
    private void checkNativeExpression(NativeExpression ne, Scope scope, boolean isInCallout) {
        if (ne instanceof BinaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ne.getChildren();

            for (NativeExpression operand : children) {
                checkNativeExpression(operand, scope, isInCallout);
            }
        } else if (ne instanceof ArrayLocation) {
            return;
        } else if (ne instanceof ScalarLocation) {
            Optional<FieldDescriptor> var = scope.getFromScope(((ScalarLocation) ne).getVariableName());
            if(var.isPresent() && var.get().getLength().isPresent() && !isInCallout)
				errors.add(new IncompatableArgumentsSemanticError("Array " + var.get().getName(), var.get().getLocationDescriptor()));
        } else if (ne instanceof MethodCall) {
            checkMethodCall((MethodCall) ne, scope);
        } else if (ne instanceof NativeLiteral) {
            return;
        } else if (ne instanceof TernaryOperation) {
            @SuppressWarnings("unchecked")
            Iterable<NativeExpression> children = (Iterable<NativeExpression>) ((TernaryOperation) ne).getChildren();

            for (NativeExpression subNE : children) {
                checkNativeExpression(subNE, scope, isInCallout);
            }
        } else if (ne instanceof UnaryOperation) {
            checkNativeExpression(((UnaryOperation) ne).getArgument(), scope, isInCallout);
        }
    }
	
	private void checkMethodCall(MethodCall mc, Scope scope){
		// Pass forward a variable telling if the current method being checked is a callout or not
		boolean isCallout = false;
		for(Callout c : prog.getCallouts()){
			if(c.getName().equals(mc.getMethodName())){
				isCallout = true;
			}
		}
		// If you have a string, and are not a callout, error
		for(GeneralExpression param : mc.getParameterValues().getChildren()){
			if(param instanceof StringLiteral){
				if(!isCallout)
					errors.add(new IncompatableArgumentsSemanticError("String " + ((StringLiteral) param).getName(), param.getLocationDescriptor()));
			} else if(param instanceof NativeExpression) {
				checkNativeExpression((NativeExpression) param, scope, isCallout);
			}
		}
	}
}
