package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

public class CompareDataFlowNode extends StatementDataFlowNode{
	
	private Scope scope;
	private NativeExpression comparison;
	
	public CompareDataFlowNode(NativeExpression comparison, Scope scope){
		super("Expression");
		this.scope = scope;
		this.comparison = comparison;
	}

	@Override
	public Scope getScope() {
		return scope;
	}
	
	@Override
	public Optional<NativeExpression> getExpression() {
		return Optional.of(comparison);
	}

	@Override
	public Collection<GeneralExpression> getExpressions() {
	    return ImmutableList.<GeneralExpression>of(comparison);
	}

	public String nodeText(){
		return comparison.asText();
	}
}
