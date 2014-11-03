package edu.mit.compilers.codegen.dataflow;

import com.google.common.base.Preconditions;

import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;

public class DataFlow {

	private final SequentialDataFlowNode beginning;
	private final SequentialDataFlowNode end;
	private final DataControlNodes controlNodes;

    public static class DataControlNodes {
        private final BranchSinkDataFlowNode breakNode;
        private final BranchSinkDataFlowNode continueNode;
        private final BranchSinkDataFlowNode returnNode;

        public DataControlNodes(BranchSinkDataFlowNode breakNode,
        		BranchSinkDataFlowNode continueNode, BranchSinkDataFlowNode returnNode) {
            this.breakNode = breakNode;
            this.continueNode = continueNode;
            this.returnNode = returnNode;
        }

        public BranchSinkDataFlowNode getBreakNode() {
            return breakNode;
        }

        public BranchSinkDataFlowNode getContinueNode() {
            return continueNode;
        }

        public BranchSinkDataFlowNode getReturnNode() {
            return returnNode;
        }
        
        public void attach(BranchSinkDataFlowNode upperBreak,
        		BranchSinkDataFlowNode upperContinue, BranchSinkDataFlowNode upperReturn){
			getBreakNode().setNext(upperBreak);
			upperBreak.setPrev(getBreakNode());
			getContinueNode().setNext(upperContinue);
			upperContinue.setPrev(getContinueNode());
			getReturnNode().setNext(upperReturn);
			upperReturn.setPrev(getReturnNode());
        }
        
        public void attach(SequentialDataFlowNode upperBreak,
        		SequentialDataFlowNode upperContinue, SequentialDataFlowNode upperReturn){
			getBreakNode().setNext(upperBreak);
			upperBreak.setPrev(getBreakNode());
			getContinueNode().setNext(upperContinue);
			upperContinue.setPrev(getContinueNode());
			getReturnNode().setNext(upperReturn);
			upperReturn.setPrev(getReturnNode());
        }
    }
	
	public DataFlow(SequentialDataFlowNode beginning, SequentialDataFlowNode end,
			DataControlNodes controlNodes){
		this.beginning = beginning;
		this.end = end;
		this.controlNodes = controlNodes;
	}
	
	public SequentialDataFlowNode getBeginning() {
		return beginning;
	}
	
	public SequentialDataFlowNode getEnd() {
		return end;
	}
	
	public DataControlNodes getControlNodes() {
		return controlNodes;
	}
	
	public static DataFlow sequenceOf(DataFlow first, DataFlow... rest){
		SequentialDataFlowNode beginning = first.getBeginning();
		SequentialDataFlowNode end = first.getEnd();
		
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		first.getControlNodes().attach(breakNode, continueNode, returnNode);
		
		for (DataFlow graph : rest) {
			// Hook ourselves upwards for control statements
			graph.getControlNodes().attach(breakNode, continueNode, returnNode);
			
			// Hook ourselves forward for the rest
			SequentialDataFlowNode nextBeginning = graph.getBeginning();
			end.setNext(nextBeginning);
			nextBeginning.setPrev(end);
			end = graph.getEnd();
		}
		
		return new DataFlow(beginning, end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}
	
	public static DataFlow sequenceOf(DataFlowFactory first, DataFlowFactory... rest){
		DataFlow[] graphs = new DataFlow[rest.length];
		for( int i = 0; i < rest.length; i++) {
			graphs[i] = rest[i].getDataFlow();
		}
		
		return sequenceOf(first.getDataFlow(), graphs);
	}
	
	public static DataFlow ofNodes(SequentialDataFlowNode... nodes){
		Preconditions.checkState(nodes.length > 0);
		SequentialDataFlowNode end = nodes[0];
		
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		
		for( int i = 1; i < nodes.length; i++ ){
			end.setNext(nodes[i]);
			nodes[i].setPrev(end);
			end = nodes[i];
		}
		
		return new DataFlow(nodes[0], end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}
	
}
