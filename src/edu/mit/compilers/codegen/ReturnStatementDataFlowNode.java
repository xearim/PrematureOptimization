package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;

public class ReturnStatementDataFlowNode extends SequentialDataFlowNode{
	
	private Scope scope;
	private ReturnStatement returnStatement;
	
	public ReturnStatementDataFlowNode(ReturnStatement returnStatement, Scope scope){
		super("Return");
		this.scope = scope;
		this.returnStatement = returnStatement;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public ReturnStatement getReturnStatement() {
		return returnStatement;
	}

}
