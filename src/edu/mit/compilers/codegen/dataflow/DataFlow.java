package edu.mit.compilers.codegen.dataflow;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import edu.mit.compilers.ast.Condition;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.MethodCallDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.ReturnStatementDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Node;

/**
 * Mutable representation of a data flow graph.
 *
 * <p>We expect to do many graph manipulations on DataFlows.  Leaving them mutable
 * is a) convenient and b) consistent with the fact that the underlying DataFlowNodes
 * are mutable.
 */
public class DataFlow {

	private SequentialDataFlowNode beginning;
	private SequentialDataFlowNode end;
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
        
        public DataControlNodes(){
        	this(new BranchSinkDataFlowNode(), new BranchSinkDataFlowNode(), new BranchSinkDataFlowNode());
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

	public void setBeginning(SequentialDataFlowNode beginning) {
	    this.beginning = beginning;
	}
	
	public SequentialDataFlowNode getEnd() {
		return end;
	}

	public void setEnd(SequentialDataFlowNode end) {
	    this.end = end;
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
    
    /**
     * Get the BcrFlowGraph representation of this DataFlow.
     *
     * <p>DataFlows are now deprecated in favor of FlowGraph<ScopedStatement>.  This
     * method exists to help us transition.
     */
    public BcrFlowGraph<ScopedStatement> asDataFlowGraph() {
        BcrFlowGraph.Builder<ScopedStatement> builder = BcrFlowGraph.builder();

        Map<DataFlowNode, Node<ScopedStatement>> newNodes =
                new HashMap<DataFlowNode, Node<ScopedStatement>>();
        // The beginning node must be added upfront, because it has no parent.  (Other nodes
        // are added when their parent is processed.)
        newNodes.put(beginning, newNode(beginning));
        // Add these other nodes upfront, too, so we're guaranteed to have a new node for
        // each terminal, even if it's unreachable.
        newNodes.put(end, newNode(end));
        newNodes.put(controlNodes.breakNode, newNode(controlNodes.breakNode));
        newNodes.put(controlNodes.continueNode, newNode(controlNodes.continueNode));
        newNodes.put(controlNodes.returnNode, newNode(controlNodes.returnNode));

        Set<DataFlowNode> visited = new HashSet<DataFlowNode>();
        Deque<DataFlowNode> agenda = new ArrayDeque<DataFlowNode>();

        agenda.push(beginning);
        while(!agenda.isEmpty()) {
            DataFlowNode node = agenda.pop();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);

            if (node instanceof BranchSinkDataFlowNode
                    || node instanceof SequentialDataFlowNode) {
                Set<DataFlowNode> successors = node.getSuccessors();

                if (!successors.isEmpty()) {
                    // Only BranchSourceDataFlowNode has multiple successors.
                    DataFlowNode next = Iterables.getOnlyElement(successors);
                    agenda.push(next);

                    if (!newNodes.containsKey(next)) {
                        newNodes.put(next, newNode(next));
                    }
                    builder.link(newNodes.get(node), newNodes.get(next));
                }
            } else if (node instanceof BranchSourceDataFlowNode) {
                BranchSourceDataFlowNode sourceNode = (BranchSourceDataFlowNode) node;

                DataFlowNode nonJumpBranch = sourceNode.getTrueBranch();
                agenda.push(nonJumpBranch);
                if (!newNodes.containsKey(nonJumpBranch)) {
                    newNodes.put(nonJumpBranch, newNode(nonJumpBranch));
                }
                builder.linkNonJumpBranch(newNodes.get(node), newNodes.get(nonJumpBranch));

                DataFlowNode jumpBranch = sourceNode.getFalseBranch();
                agenda.push(jumpBranch);
                if (!newNodes.containsKey(jumpBranch)) {
                    newNodes.put(jumpBranch, newNode(jumpBranch));
                }
                builder.linkJumpBranch(
                        newNodes.get(node), sourceNode.getType(), newNodes.get(jumpBranch));
            } else {
                throw new AssertionError("Unexpected node type for " + node);
            }
        }
        builder.setStartTerminal(newNodes.get(beginning));
        builder.setEndTerminal(newNodes.get(end));
        builder.setBreakTerminal(newNodes.get(controlNodes.breakNode));
        builder.setContinueTerminal(newNodes.get(controlNodes.continueNode));
        builder.setReturnTerminal(newNodes.get(controlNodes.returnNode));

        return builder.build();
    }

    private Node<ScopedStatement> newNode(DataFlowNode node) {
        if (node instanceof BranchSinkDataFlowNode
                || node instanceof BranchSourceDataFlowNode
                || node instanceof NopDataFlowNode) {
            return Node.nop();
        } else if (node instanceof AssignmentDataFlowNode) {
            AssignmentDataFlowNode aNode = (AssignmentDataFlowNode) node;
            return Node.of(new ScopedStatement(
                    aNode.getAssignment(),
                    aNode.getScope()));
        } else if (node instanceof CompareDataFlowNode) {
            CompareDataFlowNode cNode = (CompareDataFlowNode) node;
            return Node.of(new ScopedStatement(
                    new Condition(cNode.getExpression().get()),
                    cNode.getScope()));
        } else if (node instanceof MethodCallDataFlowNode) {
            MethodCallDataFlowNode mNode = (MethodCallDataFlowNode) node;
            return Node.of(new ScopedStatement(
                    mNode.getMethodCall(),
                    mNode.getScope()));
        } else if (node instanceof ReturnStatementDataFlowNode) {
            ReturnStatementDataFlowNode rNode = (ReturnStatementDataFlowNode) node;
            return Node.of(new ScopedStatement(
                    rNode.getReturnStatement(),
                    rNode.getScope()));
        } else {
            throw new AssertionError("Unexpected node type for " + node);
        }
    }
}
