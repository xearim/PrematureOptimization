package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;

public class MethodCallDataFlowNode extends SequentialDataFlowNode{
	
	private Scope scope;
	private MethodCall methodCall;
	
	public MethodCallDataFlowNode(MethodCall methodCall, Scope scope){
		super("methodCall");
		this.scope = scope;
		this.methodCall = methodCall;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public MethodCall getMethodCall() {
		return methodCall;
	}

}