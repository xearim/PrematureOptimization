package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Scope;

public class AssignmentDataFlowNode extends SequentialDataFlowNode{
	
	private Scope scope;
	private Assignment assignment;
	
	public AssignmentDataFlowNode(Assignment assignment, Scope scope){
		super("Assignment");
		this.scope = scope;
		this.assignment = assignment;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}

	@Override
	public Collection<GeneralExpression> getExpressions() {
	    return ImmutableList.<GeneralExpression>of(assignment.getExpression());
	}

}
