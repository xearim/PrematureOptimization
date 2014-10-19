package edu.mit.compilers.codegen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.asm.instructions.WriteLabel;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;

public class MethodBlockPrinter {
	private final Method method;
	private final BiTerminalGraph methodGraph;
	private final VisitedSet e;
	
	private class VisitedSet{
		
		private Collection<SequentialControlFlowNode> visitedNodes;
		
		public VisitedSet(){
			visitedNodes = new ArrayList<SequentialControlFlowNode>();
		}
		
		public void visit(SequentialControlFlowNode node){
			visitedNodes.add(node);
		}
		
		public boolean haveVisited(SequentialControlFlowNode node){
			for(SequentialControlFlowNode visitedNode: visitedNodes){
				if(visitedNode.getNodeID() == node.getNodeID())
					return true;
			}
			return false;
		}
	}
	
	MethodBlockPrinter(Method method) {
		this.method = method;
		this.methodGraph = new MethodGraphFactory(this.method).getGraph();
		this.e = new VisitedSet();
	}
	
	public void printStream(PrintStream outputStream){
		printNodeChain(methodGraph.getBeginning(), outputStream);
	}
	
	private void printNodeChain(ControlFlowNode beginning, PrintStream outputStream){
		Optional<ControlFlowNode> currentNodeWrapper = Optional.of(methodGraph.getBeginning());
		while(currentNodeWrapper.isPresent()){ 
			ControlFlowNode currentNode = currentNodeWrapper.get();
			if(currentNode instanceof SequentialControlFlowNode){
				if(e.haveVisited((SequentialControlFlowNode) currentNode)){
					// If we have already visited the node, it must be a label node, so we just want to jump to that label
					SequentialControlFlowNode labelNode = (SequentialControlFlowNode) currentNode;
					checkState(labelNode.hasInstruction());
					checkState(labelNode.getInstruction() instanceof WriteLabel);
					outputStream.println(Instructions.jump(((WriteLabel) labelNode.getInstruction()).getLabel()));
					// After we jump, it is pointless to write anything more, so stop recursing
					currentNodeWrapper = Optional.<ControlFlowNode>absent();
				} else {
					// Else we should go ahead and print the node and keep recursing
					printSequentialNode((SequentialControlFlowNode) currentNode, outputStream);
					currentNodeWrapper = ((SequentialControlFlowNode) currentNode).hasNext()
												? Optional.of(((SequentialControlFlowNode) currentNode).getNext())
												: Optional.<ControlFlowNode>absent();
				}
			} else if(currentNode instanceof BranchingControlFlowNode) {
				printBranchingNode((BranchingControlFlowNode) currentNode, outputStream);
			} else {
				throw new AssertionError("Bad node type in control flow graph of type: " + currentNode.getClass().toString());
			}
		}
	}
	
	private void printSequentialNode(SequentialControlFlowNode currentNode, PrintStream outputStream){
		// Visit the node, so we add it to the set
		e.visit(currentNode);
		// If it has an instruction, print that mofo
		if(currentNode.hasInstruction()){
			outputStream.println(currentNode.getInstruction().inAttSyntax());
		}
	}
	
	private void printBranchingNode(BranchingControlFlowNode currentNode, PrintStream outputStream){
		ControlFlowNode trueNode = currentNode.getTrueBranch();
		ControlFlowNode falseNode = currentNode.getFalseBranch();
		// Figure out where to jump for the false branch
		Label falseLabel = getFalseLabel((SequentialControlFlowNode) falseNode);
		// Insert our special jump over the true branch
		outputStream.println(Instructions.jumpTyped(currentNode.getType(), falseLabel));
		// Recurse on the true branch
		printNodeChain(trueNode, outputStream);
		// Then on the false branch
		printNodeChain(falseNode, outputStream);
		
	}
	
	private Label getFalseLabel(SequentialControlFlowNode falseNode){
		// The first node of a false node sequence should always be a writeLabel instruction
		checkState(falseNode.hasInstruction());
		checkState(falseNode.getInstruction() instanceof WriteLabel);
		return ((WriteLabel) falseNode.getInstruction()).getLabel();
	}
	
}
