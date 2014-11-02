package edu.mit.compilers.codegen.controllinker;

import java.util.HashMap;

import com.google.common.base.Preconditions;

import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.MethodCallDataFlowNode;
import edu.mit.compilers.codegen.ReturnStatementDataFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.statements.AssignmentGraphFactory;
import edu.mit.compilers.codegen.controllinker.statements.CompareGraphFactory;
import edu.mit.compilers.codegen.controllinker.statements.ReturnStatementGraphFactory;
import edu.mit.compilers.codegen.dataflow.DataFlow;

public class DataFlowGraphFactory implements ControlTerminalGraphFactory {

    private final ControlTerminalGraph graph;

    public DataFlowGraphFactory(DataFlow dataFlow) {
        this.graph = calculateGraph(dataFlow);
    }

    private ControlTerminalGraph calculateGraph(DataFlow dataFlow) {

    	SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
    	SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("DF cont");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("DF break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("DF return");
        
        HashMap<Long,ControlFlowNode> visited = new HashMap<Long,ControlFlowNode>();
        
        SequentialControlFlowNode end = 
        		ProcessDataFlowNode(dataFlow.getBeginning(), visited, returnNode, start);

        return new ControlTerminalGraph(start, end,
        		new ControlNodes(breakNode, continueNode, returnNode));
    }
    
    private SequentialControlFlowNode ProcessDataFlowNode(DataFlowNode currentNode, 
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	if(currentNode instanceof SequentialDataFlowNode){
    		return ProcessSequentialDataFlowNode((SequentialDataFlowNode) currentNode, 
    				   visited, returnNode, end);
    	} else if(currentNode instanceof BranchSinkDataFlowNode){
    		return ProcessBranchSinkDataFlowNode((BranchSinkDataFlowNode) currentNode, 
    					visited, returnNode, end);
    	} else if(currentNode instanceof BranchSourceDataFlowNode){
    		return ProcessBranchSourceDataFlowNode((BranchSourceDataFlowNode) currentNode, 
    				   visited, returnNode, end);
    	} else {
    		throw new AssertionError("Invalid DataFlowNode in DataFlow");
    	}
    }
    
    private SequentialControlFlowNode ProcessSequentialDataFlowNode(SequentialDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	if(currentNode instanceof AssignmentDataFlowNode){
    		return ProcessAssignmentDataFlowNode((AssignmentDataFlowNode) currentNode,
    				visited, returnNode, end);
    	} else if(currentNode instanceof CompareDataFlowNode){
    		return ProcessCompareDataFlowNode((CompareDataFlowNode) currentNode,
    				visited, returnNode, end);
    	} else if(currentNode instanceof MethodCallDataFlowNode){
    		return ProcessMethodCallDataFlowNode((MethodCallDataFlowNode) currentNode,
    				visited, returnNode, end);
    	} else if(currentNode instanceof ReturnStatementDataFlowNode){
    		return ProcessReturnStatementDataFlowNode((ReturnStatementDataFlowNode) currentNode,
    				visited, returnNode, end);
    	} else {
    		throw new AssertionError("Invalid SequentialDataFlowNode in DataFlow");
    	}
    }
    
    private SequentialControlFlowNode ProcessAssignmentDataFlowNode(AssignmentDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	// Build the assignment graph
    	BiTerminalGraph assignmentGraph = new AssignmentGraphFactory(currentNode.getAssignment().getLocation(),
    			currentNode.getAssignment().getOperation(), currentNode.getAssignment().getExpression(), 
    			currentNode.getScope()).getGraph();	
    	end.setNext(assignmentGraph.getBeginning());
    	// Put this node in our visited list, it should not already be there
    	Preconditions.checkState(visited.get(currentNode.getNodeID()) == null);
    	visited.put(currentNode.getNodeID(),assignmentGraph.getBeginning());
    	// Add the next node to the visiting set if it exists
    	if(currentNode.hasNext()){
    		return ProcessDataFlowNode(currentNode.getNext(), visited, returnNode,
    				assignmentGraph.getEnd());
    	} else {
    		return assignmentGraph.getEnd();
    	}
    }
    
    private SequentialControlFlowNode ProcessCompareDataFlowNode(CompareDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	// Build the compare graph
    	BiTerminalGraph compareGraph = new CompareGraphFactory(currentNode.getLeftArg(),
    			currentNode.getRightArg(), currentNode.getScope()).getGraph();	
    	end.setNext(compareGraph.getBeginning());
    	// Put this node in our visited list, it should not already be there
    	Preconditions.checkState(visited.get(currentNode.getNodeID()) == null);
    	visited.put(currentNode.getNodeID(),compareGraph.getBeginning());
    	// Add the next node to the visiting set if it exists
    	if(currentNode.hasNext()){
    		return ProcessDataFlowNode(currentNode.getNext(), visited, returnNode,
    				compareGraph.getEnd());
    	} else {
    		return compareGraph.getEnd();
    	}
    }
    
    private SequentialControlFlowNode ProcessMethodCallDataFlowNode(MethodCallDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	// Build the method call graph
    	BiTerminalGraph methodCallGraph = new MethodCallGraphFactory(currentNode.getMethodCall(),
    			currentNode.getScope()).getGraph();	
    	
    	end.setNext(methodCallGraph.getBeginning());
    	// Put this node in our visited list, it should not already be there
    	Preconditions.checkState(visited.get(currentNode.getNodeID()) == null);
    	visited.put(currentNode.getNodeID(),methodCallGraph.getBeginning());
    	// Add the next node to the visiting set if it exists
    	if(currentNode.hasNext()){
    		return ProcessDataFlowNode(currentNode.getNext(), visited, returnNode,
    				methodCallGraph.getEnd());
    	} else {
    		return methodCallGraph.getEnd();
    	}
    }
    
    private SequentialControlFlowNode ProcessReturnStatementDataFlowNode(ReturnStatementDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	// Build the return statement graph
    	ControlTerminalGraph returnStatementGraph = new ReturnStatementGraphFactory(currentNode.getReturnStatement(),
    			currentNode.getScope()).getGraph();	
    	// Put this node in our visited list, it should not already be there
    	Preconditions.checkState(visited.get(currentNode.getNodeID()) == null);
    	visited.put(currentNode.getNodeID(),returnStatementGraph.getBeginning());
    	// There should be no following nodes, this is a return
    	Preconditions.checkState(!currentNode.hasNext());
    	// We are just going to hook the return directly to the return node
    	returnStatementGraph.getControlNodes().getReturnNode().setNext(returnNode);
    	// And we dont recurse because we are done here
    	return returnStatementGraph.getEnd();
    }

    private SequentialControlFlowNode ProcessBranchSinkDataFlowNode(BranchSinkDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	// Check to see if we have been to this node before
    	if(visited.get(currentNode.getNodeID()) != null){
    		end.setNext(visited.get(currentNode.getNodeID()));
    		return (SequentialControlFlowNode) visited.get(currentNode.getNodeID());
    	} else {
    		SequentialControlFlowNode sink = SequentialControlFlowNode.nopTerminal();
    		visited.put(currentNode.getNodeID(), sink);
    		end.setNext(sink);
    		if(currentNode.hasNext()){
    			return ProcessDataFlowNode(currentNode.getNext(), visited, returnNode,
        				sink);
    		} else {
    			return sink;
    		}
    	}
    }
    
    private SequentialControlFlowNode ProcessBranchSourceDataFlowNode(BranchSourceDataFlowNode currentNode,
    		HashMap<Long,ControlFlowNode> visited, SequentialControlFlowNode returnNode, SequentialControlFlowNode end){
    	SequentialControlFlowNode trueBranch = SequentialControlFlowNode.nopTerminal();
    	SequentialControlFlowNode falseBranch = SequentialControlFlowNode.nopTerminal();
    	
    	BranchingControlFlowNode branch = new BranchingControlFlowNode(currentNode.getType(), trueBranch, falseBranch);
    	
    	end.setNext(branch);
    	visited.put(currentNode.getNodeID(), branch);
    	
    	SequentialControlFlowNode terminalTrue = ProcessDataFlowNode(currentNode.getTrueBranch(), visited, returnNode,
    												trueBranch);
    	SequentialControlFlowNode terminalFalse = ProcessDataFlowNode(currentNode.getFalseBranch(), visited, returnNode,
				falseBranch);
    	
    	if(visited.containsKey(terminalTrue.getNodeID())){
    		return terminalTrue;
    	} else {
    		return terminalFalse;
    	}
    }
    
    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}