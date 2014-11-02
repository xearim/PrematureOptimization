package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow.ControlNodes;

public class BreakStatementDataFlowFactory implements DataFlowFactory{
	
	public BreakStatementDataFlowFactory() {}
	
	private DataFlow calculateDataFlow() {
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("Break Start");
		SequentialDataFlowNode end = SequentialDataFlowNode.nopNamed("Break End");
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		
		start.setNext(breakNode);
		breakNode.setPrev(start);
		
		return new DataFlow(start, end,
				new ControlNodes(breakNode, continueNode, returnNode));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow();
	}
}
