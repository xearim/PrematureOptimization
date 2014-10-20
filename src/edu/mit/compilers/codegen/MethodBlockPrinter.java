package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkState;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;

public class MethodBlockPrinter {
	private final Method method;
	private final BiTerminalGraph methodGraph;
	private final Set<ControlFlowNode> multiSourced;
	private final VisitedSet e;
	
	private class VisitedSet{
		
		private Collection<ControlFlowNode> visitedNodes;
		
		public VisitedSet(){
			visitedNodes = new ArrayList<ControlFlowNode>();
		}
		
		public void visit(ControlFlowNode node){
			visitedNodes.add(node);
		}
		
		public boolean haveVisited(ControlFlowNode node){
			for(ControlFlowNode visitedNode: visitedNodes){
				if(visitedNode.getNodeID() == node.getNodeID())
					return true;
			}
			return false;
		}
	}
	
	MethodBlockPrinter(Method method) {
		this.method = method;
		this.methodGraph = new MethodGraphFactory(this.method).getGraph();
		this.multiSourced =
		        new SourceCounter().getMultiSourceNodes(this.methodGraph.getBeginning());
		this.e = new VisitedSet();
	}
	
	public void printStream(PrintStream outputStream){
	    ControlFlowNode beginning = methodGraph.getBeginning();
	    new SourceCounter().getMultiSourceNodes(beginning);
		printNodeChain(methodGraph.getBeginning(), outputStream);
	}
	
	private void printNodeChain(ControlFlowNode currentNode, PrintStream outputStream){
	    if(e.haveVisited(currentNode)){
	        // If we have already visited the node, it must be a label node, so we just want to jump to that label
	        SequentialControlFlowNode labeledNode = (SequentialControlFlowNode) currentNode;
	        checkState(multiSourced.contains(labeledNode));
	        outputStream.println(Instructions.jump(getMultiSourceLabel(labeledNode)).inAttSyntax());
	        // After we jump, it is pointless to write anything more, so stop recursing
	        return;
	    } else {
	        e.visit(currentNode);
	    }

	    // Print the node. 
	    if(currentNode instanceof SequentialControlFlowNode){
	        // Print the node and keep recursing
	        SequentialControlFlowNode seqNode = (SequentialControlFlowNode) currentNode;
	        printSequentialNode(seqNode, outputStream);
	        if (seqNode.hasNext()) {
	            printNodeChain(seqNode.getNext(), outputStream);
	        }
	    } else if(currentNode instanceof BranchingControlFlowNode) {
	        printBranchingNode((BranchingControlFlowNode) currentNode, outputStream);
	        return;
	    } else {
	        throw new AssertionError("Bad node type in control flow graph of type: " + currentNode.getClass().toString());
	    }
	}
	
	private void printSequentialNode(SequentialControlFlowNode currentNode, PrintStream outputStream){
		// Visit the node, so we add it to the set
        if (multiSourced.contains(currentNode)) {
            printLabel(getMultiSourceLabel(currentNode), outputStream);
        }
		// If it has an instruction, print that mofo
		if(currentNode.hasInstruction()){
			outputStream.println(currentNode.getInstruction().inAttSyntax());
		}
	}
	
	private void printBranchingNode(BranchingControlFlowNode currentNode, PrintStream outputStream){
		ControlFlowNode trueNode = currentNode.getTrueBranch();
		ControlFlowNode falseNode = currentNode.getFalseBranch();
		// Figure out where to jump for the false branch
		// Current node has only one false branch, so this is guaranteed to be unique.
		Label falseLabel = getFalseLabel(currentNode);
		// Insert our special jump over the true branch
		outputStream.println(Instructions.jumpTyped(currentNode.getType(), falseLabel).inAttSyntax());
		// Recurse on the true branch
		printNodeChain(trueNode, outputStream);
		// Print the label for the false node, so we can jump to it.
		printLabel(falseLabel, outputStream);
		// Then on the false branch
		printNodeChain(falseNode, outputStream);
	}
	
	private Label getFalseLabel(ControlFlowNode falseNode){
		// The first node of a false node sequence should always be a writeLabel instruction
		return new Label(LabelType.CONTROL_FLOW, falseNode.getNodeID() + "_false");
	}
	
	private Label getMultiSourceLabel(ControlFlowNode node) {
	    return new Label(LabelType.CONTROL_FLOW, node.getNodeID() +"_multi_source");
	}
	
	private void printLabel(Label label, PrintStream outputStream) {
	    outputStream.println(label.labelText() + ":");
	}
	
}
