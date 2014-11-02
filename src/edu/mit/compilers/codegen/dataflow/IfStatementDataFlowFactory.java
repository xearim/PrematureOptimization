package edu.mit.compilers.codegen.dataflow;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import edu.mit.compilers.ast.BooleanLiteral;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.dataflow.DataFlow.ControlNodes;

public class IfStatementDataFlowFactory implements DataFlowFactory {
	
	private IfStatement ifStatement;
	private Scope scope;
	
	public IfStatementDataFlowFactory(IfStatement ifStatement, Scope scope) {
		this.ifStatement = ifStatement;
		this.scope = scope;
	}
	
	private DataFlow calculateDataFlow(IfStatement ifStatement, Scope scope) {
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("ForLoop Begin");
		SequentialDataFlowNode end = SequentialDataFlowNode.nopNamed("ForLoop End");
		BranchSinkDataFlowNode endSink = new BranchSinkDataFlowNode();
		// We are going to sink control flow elements into one big node
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		
		
		// We need the comparison 
		// TODO: Name this true
		CompareDataFlowNode ifComparator = new CompareDataFlowNode(ifStatement.getCondition(),
				new BooleanLiteral("true", LocationDescriptor.machineCode()), scope);
		
		
		// The first part of the if statement
		DataFlow thenBlock = new BlockDataFlowFactory(ifStatement.getThenBlock()).getDataFlow();
		thenBlock.getControlNodes().attach(breakNode, continueNode, returnNode);
		
		// if we have an else, get the second part of the if statement
		DataFlow elseBlock = ifStatement.getElseBlock().isPresent()
				? new BlockDataFlowFactory(ifStatement.getElseBlock().get()).getDataFlow()
				: DataFlow.ofNodes(SequentialDataFlowNode.nop());
		elseBlock.getControlNodes().attach(breakNode, continueNode, returnNode);
		
		// Finally, make the branch between nodes
		BranchSourceDataFlowNode ifCmpBranch = new BranchSourceDataFlowNode(JumpType.JNE);
		
		// Time to hook everything up
		link(start,ifComparator);
		link(ifComparator, ifCmpBranch);
		link(ifCmpBranch.getTrueBranch(), thenBlock.getBeginning());
		link(thenBlock.getEnd(), endSink);
		link(ifCmpBranch.getFalseBranch(), elseBlock.getBeginning());
		link(elseBlock.getEnd(), endSink);
		link(endSink, end);
		
		return new DataFlow(start, end,
				new ControlNodes(breakNode, continueNode, returnNode));
		
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow(ifStatement, scope);
	}


}