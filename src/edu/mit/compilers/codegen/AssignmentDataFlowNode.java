package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Assignment;
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

}
