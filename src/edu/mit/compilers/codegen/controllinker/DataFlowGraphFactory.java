package edu.mit.compilers.codegen.controllinker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
    private HashSet<DataFlowNode> visited; 
    private HashMap<DataFlowNode,ControlFlowNode> conversion;
    private SequentialControlFlowNode terminal;
    private SequentialControlFlowNode returnNode, continueNode, breakNode;

    public DataFlowGraphFactory(DataFlow dataFlow) {
        this.graph = calculateGraph(dataFlow);
    }

    private ControlTerminalGraph calculateGraph(DataFlow dataFlow) {
    	SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal(); 
    	terminal = SequentialControlFlowNode.nopTerminal();
        returnNode = SequentialControlFlowNode.namedNop("DF return");
        continueNode = SequentialControlFlowNode.namedNop("DF cont");
        breakNode = SequentialControlFlowNode.namedNop("DF break");
    
        visited = new HashSet<DataFlowNode>();
        conversion = new HashMap<DataFlowNode,ControlFlowNode>();
    	
        ProcessDataFlowNode(dataFlow.getBeginning(), start);
        
        return new ControlTerminalGraph(start, terminal,
        		new ControlNodes(breakNode, continueNode, returnNode));
    }
    
    private void ProcessDataFlowNode(DataFlowNode currentNode, SequentialControlFlowNode end){
    	if(currentNode instanceof SequentialDataFlowNode){
    		ProcessSequentialDataFlowNode((SequentialDataFlowNode) currentNode, end);
    	} else if(currentNode instanceof BranchSinkDataFlowNode){
    		ProcessBranchSinkDataFlowNode((BranchSinkDataFlowNode) currentNode, end);
    	} else if(currentNode instanceof BranchSourceDataFlowNode){
    		ProcessBranchSourceDataFlowNode((BranchSourceDataFlowNode) currentNode, end);
    	} else {
    		throw new AssertionError("Invalid DataFlowNode in DataFlow");
    	}
    }
    
    private void ProcessSequentialDataFlowNode(SequentialDataFlowNode currentNode, SequentialControlFlowNode end){
    	if(currentNode instanceof AssignmentDataFlowNode){
    		ProcessAssignmentDataFlowNode((AssignmentDataFlowNode) currentNode, end);
    	} else if(currentNode instanceof CompareDataFlowNode){
    		ProcessCompareDataFlowNode((CompareDataFlowNode) currentNode, end);
    	} else if(currentNode instanceof MethodCallDataFlowNode){
    		ProcessMethodCallDataFlowNode((MethodCallDataFlowNode) currentNode, end);
    	} else if(currentNode instanceof ReturnStatementDataFlowNode){
    		ProcessReturnStatementDataFlowNode((ReturnStatementDataFlowNode) currentNode, end);
    	} else {
    		ProcessNopDataFlowNode(currentNode, end);
    	}
    }
    
    private void ProcessAssignmentDataFlowNode(AssignmentDataFlowNode currentNode, 
    		SequentialControlFlowNode end){
    	// Build the assignment graph
    	BiTerminalGraph assignmentGraph = new AssignmentGraphFactory(currentNode.getAssignment().getLocation(),
    			currentNode.getAssignment().getOperation(), currentNode.getAssignment().getExpression(), 
    			currentNode.getScope(), currentNode.getAssignment().getFromCompiler()).getGraph();	
    	end.setNext(assignmentGraph.getBeginning());
    	// Recurse if we can, else we have hit the end, set the final node
    	if(currentNode.hasNext()){
    		ProcessDataFlowNode(currentNode.getNext(), assignmentGraph.getEnd());
    	} else {
    		assignmentGraph.getEnd().setNext(terminal);
    	}
    }
    
    private void ProcessCompareDataFlowNode(CompareDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	// Build the compare graph
    	BiTerminalGraph compareGraph = new CompareGraphFactory(currentNode.getLeftArg(),
    			currentNode.getRightArg(), currentNode.getScope()).getGraph();	
    	end.setNext(compareGraph.getBeginning());
    	// Recurse if we can, else we have hit the end, set the final node
    	if(currentNode.hasNext()){
    		ProcessDataFlowNode(currentNode.getNext(), compareGraph.getEnd());
    	} else {
    		compareGraph.getEnd().setNext(terminal);
    	}
    }
    
    private void ProcessMethodCallDataFlowNode(MethodCallDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	// Build the method call graph
    	BiTerminalGraph methodCallGraph = new MethodCallGraphFactory(currentNode.getMethodCall(),
    			currentNode.getScope()).getGraph();	
    	end.setNext(methodCallGraph.getBeginning());
    	// Recurse if we can, else we have hit the end, set the final node
    	if(currentNode.hasNext()){
    		ProcessDataFlowNode(currentNode.getNext(), methodCallGraph.getEnd());
    	} else {
    		methodCallGraph.getEnd().setNext(terminal);
    	}
    }
    
    private void ProcessReturnStatementDataFlowNode(ReturnStatementDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	// Build the return statement graph
    	ControlTerminalGraph returnStatementGraph = new ReturnStatementGraphFactory(currentNode.getReturnStatement(),
    			currentNode.getScope()).getGraph();	
    	end.setNext(returnStatementGraph.getBeginning());
    	returnStatementGraph.getControlNodes().getReturnNode().setNext(returnNode);
    	// Recurse if we can, else we have hit the end, set the final node
    	if(currentNode.hasNext()){
    		ProcessDataFlowNode(currentNode.getNext(), returnStatementGraph.getEnd());
    	} else {
    		returnStatementGraph.getEnd().setNext(terminal);
    	}
    }
    
    private void ProcessNopDataFlowNode(SequentialDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	// Now just go forward
    	if(currentNode.hasNext()){
    		ProcessDataFlowNode(currentNode.getNext(), end);
    	} else {
    		end.setNext(terminal);
    	}
    }

    private void ProcessBranchSinkDataFlowNode(BranchSinkDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	// Check to see if we have been to this node before
    	if(visited.contains(currentNode)){
    		end.setNext(conversion.get(currentNode));
    	} else {
    		SequentialControlFlowNode sink = SequentialControlFlowNode.namedNop("Sink");
    		end.setNext(sink);
    		visited.add(currentNode);
    		conversion.put(currentNode, sink);
    		if(currentNode.hasNext()){
    			ProcessDataFlowNode(currentNode.getNext(), sink);
    		} else {
    			sink.setNext(terminal);
    		}
    	}
    }
    
    private void ProcessBranchSourceDataFlowNode(BranchSourceDataFlowNode currentNode,
    		SequentialControlFlowNode end){
    	SequentialControlFlowNode trueBranch = SequentialControlFlowNode.namedNop("True Branch");
    	SequentialControlFlowNode falseBranch = SequentialControlFlowNode.namedNop("False Branch");
    	BranchingControlFlowNode branch = new BranchingControlFlowNode(currentNode.getType(), trueBranch, falseBranch);
    	end.setNext(branch);
    	// Only one of these will set terminal, because eventually all control flow
    	// is re-unified at the bottom
    	ProcessDataFlowNode(currentNode.getTrueBranch(), trueBranch);
    	ProcessDataFlowNode(currentNode.getFalseBranch(), falseBranch);
    }
    
    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}