package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow.DataControlNodes;

public class ContinueStatementDataFlowFactory implements DataFlowFactory{
	
	public ContinueStatementDataFlowFactory() {}
	
	private DataFlow calculateDataFlow() {
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("Continue Start");
		SequentialDataFlowNode end = SequentialDataFlowNode.nopNamed("Continue End");
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		
		start.setNext(continueNode);
		continueNode.setPrev(start);
		
		return new DataFlow(start, end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow();
	}
}