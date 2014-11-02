package edu.mit.compilers.codegen.dataflow;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow.ControlNodes;

public class BlockDataFlowFactory implements DataFlowFactory{
	private Block block;
	
	public BlockDataFlowFactory(Block block){
		this.block = block;
	}
	
	private DataFlow calculateDataFlow(Block block){
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("Block Begin");
		SequentialDataFlowNode end = SequentialDataFlowNode.nopNamed("Block End");
		// We are going to sink control flow elements into one big node
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();


		// We are going to explicitly set variables to 0, optimizations should
		// Get rid of the unnecessary assignments		
		SequentialDataFlowNode currentNode = start;
		for(FieldDescriptor variable: block.getScope().getVariables()){
			DataFlow nextDataFlow = zeroOutVariable(variable, block.getScope());
			
			nextDataFlow.getControlNodes().attach(breakNode, continueNode, returnNode);
			
			link(currentNode, nextDataFlow.getBeginning());
			currentNode = nextDataFlow.getEnd();
		}
		
		for(Statement statement : block.getStatements()) {
			DataFlow statementDataFlow = 
					new StatementDataFlowFactory(statement, block.getScope()).getDataFlow();
			
			// Hook in the control flow elements
			statementDataFlow.getControlNodes().attach(breakNode, continueNode, returnNode);
			
			// Hook ourselves to the previous
			link(currentNode, statementDataFlow.getBeginning());
			currentNode = statementDataFlow.getEnd();
		}
		
		currentNode.setNext(end);
		end.setPrev(currentNode);
		
		return new DataFlow(start, end,
				new ControlNodes(breakNode, continueNode, returnNode));
	}
	
	private DataFlow zeroOutVariable(FieldDescriptor variable, Scope scope){
		if(variable.getLength().isPresent()){
			return zeroOutArray(variable, scope);
		} else {
			return zeroOutScalar(variable, scope);
		}
	}
	
	private DataFlow zeroOutArray(FieldDescriptor variable, Scope scope){
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("Zero Out Array " + variable.getVariable().toString());
		
		DataFlow zeroOut = DataFlow.ofNodes(start);
		for( int i = 0; i < variable.getLength().get().get64BitValue(); i++){
			// For each array element
			ArrayLocation arrayVariable = new ArrayLocation(variable.getVariable(),
					new IntLiteral(Long.toString(i), LocationDescriptor.machineCode()), LocationDescriptor.machineCode());
			// Make a zero
			NativeExpression zero = new IntLiteral(Long.toString(0), LocationDescriptor.machineCode());
			// create a new assignment instruction setting the var = 0
			Assignment assignment = new Assignment(arrayVariable, AssignmentOperation.SET_EQUALS,
					zero, LocationDescriptor.machineCode());
			
			zeroOut = DataFlow.sequenceOf(zeroOut, 
										  DataFlow.ofNodes(new AssignmentDataFlowNode(assignment, scope)));
			
		}
		return zeroOut;
	}
	
	private DataFlow zeroOutScalar(FieldDescriptor variable, Scope scope){
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("Zero Out Variable " + variable.getVariable().toString());
		
		// Take the variable
		ScalarLocation scalarVariable = new ScalarLocation(variable.getVariable(),
				LocationDescriptor.machineCode());
		// Make a zero
		NativeExpression zero = new IntLiteral(Long.toString(0), LocationDescriptor.machineCode());
		// Make an assignment setting the var = 0
		Assignment assignment = new Assignment(scalarVariable, AssignmentOperation.SET_EQUALS,
				zero, LocationDescriptor.machineCode());
		
		return DataFlow.ofNodes(start,
								new AssignmentDataFlowNode(assignment, scope));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow(block);
	}

}
