package edu.mit.compilers.codegen.dataflow;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow.DataControlNodes;
import edu.mit.compilers.common.Variable;

public class BlockDataFlowFactory implements DataFlowFactory{
	private Block block;
	
	public BlockDataFlowFactory(Block block){
		this.block = block;
	}
	
	private DataFlow calculateDataFlow(Block block){
		SequentialDataFlowNode start = NopDataFlowNode.nopNamed("Block Begin");
		SequentialDataFlowNode end = NopDataFlowNode.nopNamed("Block End");
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
		
		link(currentNode, end);
		
		return new DataFlow(start, end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}
	
	private DataFlow zeroOutVariable(FieldDescriptor variable, Scope scope){
		if(variable.getLength().isPresent()){
			return zeroOutArray(variable, scope);
		} else {
			return zeroOutScalar(variable, scope);
		}
	}
	
	private DataFlow zeroOutArray(FieldDescriptor variable, Scope scope){		
		// Build our custom loop variable for the end of loop range
		FieldDescriptor zeroIndex = new FieldDescriptor(Variable.forCompiler("ZeroingVar"), BaseType.INTEGER,
				LocationDescriptor.machineCode());
		
		// We make a scope for that variable so we can "find" it
		Scope zeroScope = new Scope(ImmutableList.of(zeroIndex),scope);
		
		// Here is our shiny new end of loop variable
		ScalarLocation zeroIndexVar = new ScalarLocation(zeroIndex.getVariable(), 
				LocationDescriptor.machineCode());
		
		// Construct the array access with the variable
		ArrayLocation arrayVariable = new ArrayLocation(variable.getVariable(),
				zeroIndexVar, LocationDescriptor.machineCode());
		// Make a zero
		NativeExpression zero = new IntLiteral(0L);
		// create a new assignment instruction setting the var = 0
		Assignment assignment = new Assignment(arrayVariable, AssignmentOperation.SET_EQUALS,
				zero, LocationDescriptor.machineCode(), false);
		
		ForLoop zeroLoop = new ForLoop(
				zeroIndexVar,
				new IntLiteral(0L),
				new IntLiteral(variable.getLength().get().get64BitValue()),
				new Block("", new Scope(ImmutableList.<FieldDescriptor>of(),zeroScope)
					, ImmutableList.<Statement>of(assignment), LocationDescriptor.machineCode()),
				LocationDescriptor.machineCode()
				);
		
		return new ForLoopDataFlowFactory(zeroLoop, zeroScope).getDataFlow();
	}
	
	private DataFlow zeroOutScalar(FieldDescriptor variable, Scope scope){
		SequentialDataFlowNode start = NopDataFlowNode.nopNamed("Zero Out Variable " + variable.getVariable().toString());
		
		// Take the variable
		ScalarLocation scalarVariable = new ScalarLocation(variable.getVariable(),
				LocationDescriptor.machineCode());
		// Make a zero
		NativeExpression zero = new IntLiteral(0L);
		// Make an assignment setting the var = 0
		Assignment assignment = new Assignment(scalarVariable, AssignmentOperation.SET_EQUALS,
				zero, LocationDescriptor.machineCode(), true);
		
		return DataFlow.ofNodes(start,
								new AssignmentDataFlowNode(assignment, scope));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow(block);
	}

}
