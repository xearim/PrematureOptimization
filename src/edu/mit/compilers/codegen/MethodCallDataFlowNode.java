package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;
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

	@Override
	public Scope getScope() {
		return scope;
	}
	
	public MethodCall getMethodCall() {
		return methodCall;
	}

    @Override
    public Collection<GeneralExpression> getExpressions() {
        return ImmutableList.<GeneralExpression>of(methodCall);
    }

}