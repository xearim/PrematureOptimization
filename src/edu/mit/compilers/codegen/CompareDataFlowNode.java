package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

public class CompareDataFlowNode extends SequentialDataFlowNode{
	
	private Scope scope;
	private NativeExpression leftArg;
	private NativeExpression rightArg;
	
	public CompareDataFlowNode(NativeExpression leftArg,
			NativeExpression rightArg, Scope scope){
		super("Expression");
		this.scope = scope;
		this.leftArg = leftArg;
		this.rightArg = rightArg;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public NativeExpression getLeftArg() {
		return leftArg;
	}
	
	public NativeExpression getRightArg() {
		return rightArg;
	}

}
