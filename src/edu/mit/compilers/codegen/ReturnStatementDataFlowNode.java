package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
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

	@Override
	public Collection<GeneralExpression> getExpressions() {
	    Optional<NativeExpression> returnValue = returnStatement.getValue();
	    return returnValue.isPresent()
	            ? ImmutableList.<GeneralExpression>of(returnValue.get())
	            : ImmutableList.<GeneralExpression>of();
	}
}
