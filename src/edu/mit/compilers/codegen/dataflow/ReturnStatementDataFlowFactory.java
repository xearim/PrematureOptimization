package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.ReturnStatementDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow.DataControlNodes;

public class ReturnStatementDataFlowFactory implements DataFlowFactory{
	
	private ReturnStatement rs;
	private Scope scope;
	
	public ReturnStatementDataFlowFactory(ReturnStatement rs, Scope scope) {
		this.rs = rs;
		this.scope = scope;
	}
	
	private DataFlow calculateDataFlow() {
		SequentialDataFlowNode start = NopDataFlowNode.nopNamed("Return Start");
		SequentialDataFlowNode end = NopDataFlowNode.nopNamed("Return End");
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		
			// Generate our return statement
			ReturnStatementDataFlowNode returnValue = new ReturnStatementDataFlowNode(rs, scope);
			
			// Hook it in-between the start and return nodes
			start.setNext(returnValue);
			returnValue.setPrev(start);
			returnValue.setNext(returnNode);
			returnNode.setPrev(returnValue);
		
		return new DataFlow(start, end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow();
	}
}