package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.increment;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.writeLabel;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.BlockGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class WhileLoopGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public WhileLoopGraphFactory(WhileLoop whileLoop, Scope scope) {
        this.graph = calculateGraph(whileLoop,scope);
    }

    private ControlTerminalGraph calculateGraph(WhileLoop whileLoop, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal(); 
        SequentialControlFlowNode loopStart = SequentialControlFlowNode.nopTerminal();

        // Create the two labels we need to have a for loop
        // One label for the beginning of the loop after initialization
        Label startL = new Label(LabelType.CONTROL_FLOW, "startwhile" + whileLoop.getID());
        SequentialControlFlowNode startLabel = SequentialControlFlowNode.terminal(writeLabel(startL));
        
        // A second label for the end of the loop
        Label endL = new Label(LabelType.CONTROL_FLOW, "endwhile" + whileLoop.getID());
        SequentialControlFlowNode endLabel = SequentialControlFlowNode.terminal(writeLabel(endL));
        
        // Link up the start to its label
        startLabel.setNext(loopStart);
        BiTerminalGraph loopStartTarget = new BiTerminalGraph(startLabel, loopStart);
        
        // And link the end to its label
        endLabel.setNext(end);
        BiTerminalGraph endTarget = new BiTerminalGraph(endLabel, end);
        
    	// Assign the max Rep var
        BiTerminalGraph maxRepetitionsAssigner = BiTerminalGraph.ofInstructions(
        											push(Literal.INITIAL_VALUE)
        											);
        // Remove the max Rep var
        BiTerminalGraph maxRepetitionsDestroyer = BiTerminalGraph.ofInstructions(
				pop(Register.R10) // pop our max rep var off the stack
				);
        
        // Comparisons for the loop
        BiTerminalGraph loopComparator = BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(whileLoop.getCondition(), scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10), // Evaluated conditional.
                        move(Literal.TRUE, Register.R11), // true.
                        compareFlagged(Register.R10, Register.R11)));

        // increment the max Repetitions var towards maxRepetitions
        BiTerminalGraph incrementor = BiTerminalGraph.ofInstructions(
                pop(Register.R10),
                increment(Register.R10),
                push(Register.R10));
        
        // The actual execution body
        ControlTerminalGraph body = new BlockGraphFactory(whileLoop.getBody()).getGraph();
        
        // The branch for checking repetition number
        BranchingControlFlowNode maxRepetitionBranch = new BranchingControlFlowNode(JumpType.JGE, 
        								loopComparator.getBeginning(), endTarget.getEnd());

        // The branch for checking conditionals
        BranchingControlFlowNode conditionalBranch = new BranchingControlFlowNode(JumpType.JNE, 
        									body.getBeginning(), endTarget.getEnd());
        
        if(whileLoop.getMaxRepetitions().isPresent()){
        	// We need a third label for max rep loops, this one takes us to the incrementor
        	SequentialControlFlowNode incLabel = SequentialControlFlowNode.terminal(writeLabel(
						new Label(LabelType.CONTROL_FLOW, "incwhile" + whileLoop.getID())));
        	
        	// Link up the incrementor to its label
            incLabel.setNext(incrementor.getBeginning());
            BiTerminalGraph incTarget = new BiTerminalGraph(incLabel, incrementor.getEnd());
        	
        	// When you have max repetitions, you need to actually pop off that variable before returning.
	        returnNode = SequentialControlFlowNode.terminal(pop(Register.R10));
	        
	        // Comparisons for the max Repetitions
	        BiTerminalGraph maxRepetitionsComparator = BiTerminalGraph.ofInstructions(
	                        pop(Register.R10), // max repetitions counter
	                        move(new Literal(whileLoop.getMaxRepetitions().get().get64BitValue()), Register.R11), // max repetitions
	                        compareFlagged(Register.R10, Register.R11));
	        
	        // insert the removal of the max repetitions stack object into the endTarget
	        endLabel.setNext(maxRepetitionsDestroyer.getBeginning());
	        maxRepetitionsDestroyer.getEnd().setNext(end);
	        
	        // All the nodes have been made.  Make the connections.
	        start.setNext(maxRepetitionsAssigner.getBeginning());
	        maxRepetitionsAssigner.getEnd().setNext(loopStartTarget.getBeginning());
	        loopStartTarget.getEnd().setNext(maxRepetitionsComparator.getBeginning());
	        maxRepetitionsComparator.getEnd().setNext(maxRepetitionBranch);
	        loopComparator.getEnd().setNext(conditionalBranch);
	        body.getEnd().setNext(incTarget.getBeginning());
	        incTarget.getEnd().setNext(loopStartTarget.getBeginning());
	        body.getControlNodes().getBreakNode().setNext(endTarget.getBeginning());
	        body.getControlNodes().getContinueNode().setNext(incTarget.getBeginning());
	        body.getControlNodes().getReturnNode().setNext(returnNode);
        } else {
        	// All the nodes have been made.  Make the connections.
	        start.setNext(loopStartTarget.getBeginning());
	        loopStartTarget.getEnd().setNext(loopComparator.getBeginning());
	        loopComparator.getEnd().setNext(conditionalBranch);
	        body.getEnd().setNext(loopStartTarget.getBeginning());
	        body.getControlNodes().getBreakNode().setNext(endTarget.getBeginning());
	        body.getControlNodes().getContinueNode().setNext(loopStartTarget.getBeginning());
	        body.getControlNodes().getReturnNode().setNext(returnNode);
        }

        return new ControlTerminalGraph(start, endTarget.getEnd(),
                	new ControlNodes(breakNode, continueNode, returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
