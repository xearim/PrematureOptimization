package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Architecture;

public class AssignmentDataFlowNode extends StatementDataFlowNode{
	
	private Scope scope;
	private Assignment assignment;
	
	public AssignmentDataFlowNode(Assignment assignment, Scope scope){
		super("Assignment");
		this.scope = scope;
		// TODO (xearim) make the ordering injection not so janky
		//this.assignment = assignment;
		this.assignment = new Assignment(assignment.getLocation(), assignment.getOperation(),
				Architecture.EXPRESSION_ORDERING.order(assignment.getExpression()), assignment.getLocationDescriptor());
	}

	@Override
	public Scope getScope() {
		return scope;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}

	@Override
	public Optional<NativeExpression> getExpression() {
	    return Optional.of(assignment.getExpression());
	}

	@Override
	public Collection<GeneralExpression> getExpressions() {
	    return ImmutableList.<GeneralExpression>of(assignment.getExpression());
	}
	
	public String nodeText(){
		return assignment.asText();
	}

	@Override
	public String toString() {
	    return "AssignmentDFN[" + assignment.toString() + "]";
	}

}
