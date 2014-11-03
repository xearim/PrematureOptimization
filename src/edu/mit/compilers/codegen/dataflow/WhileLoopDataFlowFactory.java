package edu.mit.compilers.codegen.dataflow;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BooleanLiteral;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSinkDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.dataflow.DataFlow.ControlNodes;
import edu.mit.compilers.common.Variable;

public class WhileLoopDataFlowFactory implements DataFlowFactory{
	
	private WhileLoop whileLoop;
	private Scope scope;
	
	public WhileLoopDataFlowFactory(WhileLoop whileLoop, Scope scope) {
		this.whileLoop = whileLoop;
		this.scope = scope;
	}
	
	private DataFlow calculateDataFlow(WhileLoop whileLoop, Scope scope) {
		SequentialDataFlowNode start = SequentialDataFlowNode.nopNamed("WhileLoop Begin");
		SequentialDataFlowNode end = SequentialDataFlowNode.nopNamed("WhileLoop End");
		BranchSinkDataFlowNode endSink = new BranchSinkDataFlowNode();
		// We are going to sink control flow elements into one big node
		BranchSinkDataFlowNode continueNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode breakNode = new BranchSinkDataFlowNode();
		BranchSinkDataFlowNode returnNode = new BranchSinkDataFlowNode();
		// We need to be able to restart a for loop
		BranchSinkDataFlowNode loopStart = new BranchSinkDataFlowNode();
		// And we need to be able to continue it
		BranchSinkDataFlowNode incrementStart = new BranchSinkDataFlowNode();
		
		// Build our custom loop variable for the max repetitions
		FieldDescriptor maxReps = new FieldDescriptor(Variable.forCompiler("WhileLoopMaxReps"), BaseType.INTEGER,
				LocationDescriptor.machineCode());
		
		// We make a scope for that variable so we can "find" it
		Scope whileLoopScope = new Scope(ImmutableList.of(maxReps),scope);
		
		// Here is our shiny new max repetitions variable
		ScalarLocation maxRepsVar = new ScalarLocation(maxReps.getVariable(), 
				LocationDescriptor.machineCode());
		
		// Lets give max reps an initial value
		// TODO: Name this 0 too
		AssignmentDataFlowNode setMaxReps = new AssignmentDataFlowNode(
				new Assignment(maxRepsVar, AssignmentOperation.SET_EQUALS,
				new IntLiteral(Long.toString(0), LocationDescriptor.machineCode()), 
				LocationDescriptor.machineCode()), whileLoopScope);
		
		// We need the comparison we would usually do for being at the end of the loop
		// TODO: Name this true as well
		CompareDataFlowNode loopComparator = new CompareDataFlowNode(whileLoop.getCondition(),
				new BooleanLiteral("true", LocationDescriptor.machineCode()), scope);
		
		// And Max Reps comparisons too
		CompareDataFlowNode maxRepComparator = new CompareDataFlowNode(maxRepsVar,
				whileLoop.getMaxRepetitions().isPresent()
				? whileLoop.getMaxRepetitions().get()
				: new IntLiteral(Long.toString(0), LocationDescriptor.machineCode()),
				scope);
		
		// And the incrementing step for max reps at the end of the while
		// TODO:Name this 1 plz
		AssignmentDataFlowNode increment = new AssignmentDataFlowNode(
				new Assignment(maxRepsVar, AssignmentOperation.PLUS_EQUALS,
				new IntLiteral(Long.toString(1), LocationDescriptor.machineCode()),
				LocationDescriptor.machineCode()), scope);
		
		// The body of the for loop
		DataFlow body = new BlockDataFlowFactory(whileLoop.getBody()).getDataFlow();
		
		// Finally the branch at the beginning of the loop
		BranchSourceDataFlowNode loopCmpBranch = new BranchSourceDataFlowNode(JumpType.JNE);
		
		// And the branch for the max Reps variable
		BranchSourceDataFlowNode maxRepsBranch = new BranchSourceDataFlowNode(JumpType.JGE);
		
		// Time to hook everything up
		if(whileLoop.getMaxRepetitions().isPresent()){
			link(start, setMaxReps);
			link(setMaxReps, loopStart);
			link(loopStart, maxRepComparator);
			link(maxRepComparator, maxRepsBranch);
			maxRepsBranch.setTrueBranch(loopComparator);
			loopComparator.setPrev(maxRepsBranch.getTrueBranch());
			maxRepsBranch.setFalseBranch(endSink);
			endSink.setPrev(maxRepsBranch.getFalseBranch());
			link(loopComparator, loopCmpBranch);
			loopCmpBranch.setTrueBranch(body.getBeginning());
			body.getBeginning().setPrev(loopCmpBranch.getTrueBranch());
			loopCmpBranch.setFalseBranch(endSink);
			endSink.setPrev(loopCmpBranch.getFalseBranch());
			link(body.getEnd(), incrementStart);
			link(incrementStart, increment);
			link(increment, loopStart);
			link(endSink, end);
		} else {
			link(start, loopStart);
			link(loopStart, loopComparator);
			link(loopComparator, loopCmpBranch);
			loopCmpBranch.setTrueBranch(body.getBeginning());
			body.getBeginning().setPrev(loopCmpBranch.getTrueBranch());
			loopCmpBranch.setFalseBranch(endSink);
			endSink.setPrev(loopCmpBranch.getFalseBranch());
			link(body.getEnd(), incrementStart);
			link(incrementStart, increment);
			link(increment, loopStart);
			link(endSink, end);
		}
		
		body.getControlNodes().attach(endSink, incrementStart, returnNode);
		
		return new DataFlow(start, end,
				new ControlNodes(breakNode, continueNode, returnNode));
		
	}

	@Override
	public DataFlow getDataFlow() {
		return calculateDataFlow(whileLoop, scope);
	}

}
