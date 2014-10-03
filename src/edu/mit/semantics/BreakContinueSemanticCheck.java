package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.semantics.errors.BreakContinueSemanticError;
import edu.mit.semantics.errors.SemanticError;

public class BreakContinueSemanticCheck implements SemanticCheck{

	Program prog;
    List<SemanticError> errors = new ArrayList<SemanticError>();
    
    public BreakContinueSemanticCheck(Program prog) {
        this.prog = prog;
    }
    
	@Override
	public List<SemanticError> doCheck() {
		Iterable<Method> methods = (Iterable<Method>) this.prog.getMethods().getChildren();

        for (Method method: methods) {
            checkBlock(method.getBlock(), false);
        }
        
        return errors;
	}
	
	// Valid is a handoff boolean, it starts off false as a method begins not in a for/while loop
	// then it is changed to true when recursing in a for/while loop, and just handed forward for
	// and if block.  Thus if you get to a break or continue and valid != true, then you are not recursing
	// inside a for/while and you should error
	private void checkBlock(Block block, boolean valid) {

		// recurse down blocks, making valid if in a while/for loop
        for (Statement stmt : block.getStatements()) {
            if (stmt instanceof Assignment){
                continue;
            } else if (stmt instanceof BreakStatement) {
            	// error if not valid
            	if(!valid)
            		errors.add(new BreakContinueSemanticError(((BreakStatement) stmt).getName(),
            												  ((BreakStatement) stmt).getLocationDescriptor()));
                continue;
            } else if (stmt instanceof ContinueStatement) {
            	// error if not valid
            	if(!valid)
            		errors.add(new BreakContinueSemanticError(((ContinueStatement) stmt).getName(),
            												  ((ContinueStatement) stmt).getLocationDescriptor()));
                continue;
            } else if (stmt instanceof ForLoop) {
            	// now valid, in a ForLoop
            	Iterable<Block> subBlocks = stmt.getBlocks();
                for (Block subBlock: subBlocks){
                    checkBlock(subBlock, true);
                }
            } else if (stmt instanceof IfStatement) {
            	// valid iff containing block is valid
            	Iterable<Block> subBlocks = stmt.getBlocks();
                for (Block subBlock: subBlocks){
                    checkBlock(subBlock, valid);
                }
            } else if (stmt instanceof MethodCall) {
            	continue;
            } else if (stmt instanceof ReturnStatement) {
                continue;
            } else if (stmt instanceof WhileLoop) {
            	// now valid, in a WhileLoop
            	Iterable<Block> subBlocks = stmt.getBlocks();
                for (Block subBlock: subBlocks){
                    checkBlock(subBlock, true);
                }
            } else {
                // TODO(Manny) throw error
            }
        }
    }

}
