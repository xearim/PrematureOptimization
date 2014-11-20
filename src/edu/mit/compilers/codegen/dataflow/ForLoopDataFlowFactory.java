package edu.mit.compilers.codegen.dataflow;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.dataflow.DataFlow.DataControlNodes;
import edu.mit.compilers.common.Variable;

public class ForLoopDataFlowFactory implements DataFlowFactory{
	
	private ForLoop forLoop;
	private Scope scope;
	
	public ForLoopDataFlowFactory(ForLoop forLoop, Scope scope) {
		this.forLoop = forLoop;
		this.scope = scope;
	}
	
	private DataFlow calculateDataFlow(ForLoop forLoop, Scope scope) {
		SequentialDataFlowNode start = NopDataFlowNode.nopNamed("ForLoop Begin");
		SequentialDataFlowNode end = NopDataFlowNode.nopNamed("ForLoop End");
		BranchSinkDataFlowNode endSink = new BranchSinkDataFlowNode();
		// We are going to sink control flow elements into one big node
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		// We need to be able to restart a for loop
		BranchSinkDataFlowNode loopStart = new BranchSinkDataFlowNode();
		// And we need to be able to continue it
		BranchSinkDataFlowNode incrementStart = new BranchSinkDataFlowNode();
		
		// The variable we loop over
		ScalarLocation loopingVar = forLoop.getLoopVariable();
		
		// Build our custom loop variable for the end of loop range
		FieldDescriptor rangeEnd = new FieldDescriptor(Variable.forCompiler("ForLoopRangeEnd"), BaseType.INTEGER,
				LocationDescriptor.machineCode());
		
		// We make a scope for that variable so we can "find" it
		Scope forLoopScope = new Scope(ImmutableList.of(rangeEnd),scope);
		
		// Here is our shiny new end of loop variable
		ScalarLocation rangeEndVar = new ScalarLocation(rangeEnd.getVariable(), 
				LocationDescriptor.machineCode());
		
		NativeExpression comparison = new BinaryOperation(BinaryOperator.LESS_THAN,
		        loopingVar, rangeEndVar,LocationDescriptor.machineCode());

		// Lets give it its proper value
		AssignmentDataFlowNode setRangeEnd = new AssignmentDataFlowNode(
				new Assignment(rangeEndVar, AssignmentOperation.SET_EQUALS,
				forLoop.getRangeEnd(), LocationDescriptor.machineCode()), forLoopScope);
		
		// We also want to set the loopingVar to its initial value
		AssignmentDataFlowNode setLooping = new AssignmentDataFlowNode(
				new Assignment(loopingVar, AssignmentOperation.SET_EQUALS,
				forLoop.getRangeStart(), LocationDescriptor.machineCode()), scope);
		
		// We need the comparison we would usually do for being at the end of the loop
		CompareDataFlowNode loopComparator = new CompareDataFlowNode(comparison, forLoopScope);
		
		// And the incrementing step at the end of the loop
		// TODO:Name this 1 plz
		AssignmentDataFlowNode increment = new AssignmentDataFlowNode(
				new Assignment(loopingVar, AssignmentOperation.PLUS_EQUALS,
				new IntLiteral(Long.toString(1), LocationDescriptor.machineCode()),
				LocationDescriptor.machineCode()), scope);
		
		// The body of the for loop
		DataFlow body = new BlockDataFlowFactory(forLoop.getBody()).getDataFlow();
		
		// Finally the branch at the beginning of the loop
		BranchSourceDataFlowNode loopCmpBranch = new BranchSourceDataFlowNode(JumpType.JNE);
		
		// Time to hook everything up
		link(start,setLooping);
		link(setLooping,setRangeEnd);
		link(setRangeEnd,loopStart);
		link(loopStart,loopComparator);
		link(loopComparator,loopCmpBranch);
		
		loopCmpBranch.setTrueBranch(body.getBeginning());
		body.getBeginning().setPrev(loopCmpBranch);
		loopCmpBranch.setFalseBranch(endSink);
		endSink.setPrev(loopCmpBranch);
		
		link(endSink, end);
		link(body.getEnd(), incrementStart);
		link(incrementStart, increment);
		link(increment, loopStart);
		
		body.getControlNodes().attach(endSink, incrementStart, returnNode);
		
		return new DataFlow(start, end,
				new DataControlNodes(breakNode, continueNode, returnNode));
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow(forLoop, scope);
	}

}
