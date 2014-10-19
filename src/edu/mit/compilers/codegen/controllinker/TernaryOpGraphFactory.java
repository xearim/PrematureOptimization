package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.writeLabel;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;

public class TernaryOpGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;
    
    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope) {
        this.graph = calculateGraph(operation, scope);
    }

    private BiTerminalGraph calculateGraph(TernaryOperation operation, Scope scope) {
    	
    	// Construct a label for the false branch
    	SequentialControlFlowNode falseLabel = SequentialControlFlowNode.terminal(writeLabel(
							new Label(LabelType.CONTROL_FLOW, "falseTernary" + operation.getID())));
    	// And make a label for the end of the ternary
    	SequentialControlFlowNode endLabel = SequentialControlFlowNode.terminal(writeLabel(
				new Label(LabelType.CONTROL_FLOW, "endTernary" + operation.getID())));
    	
    	
    	BiTerminalGraph trueBranch = new NativeExprGraphFactory(operation.getTrueResult(), scope).getGraph();
    	BiTerminalGraph falseBranch = new NativeExprGraphFactory(operation.getFalseResult(), scope).getGraph();
    	
    	// Hook up the true target
    	trueBranch.getEnd().setNext(endLabel);
    	BiTerminalGraph trueTarget = new BiTerminalGraph(trueBranch.getBeginning(), endLabel);
    	
    	// Hook up the false target
    	falseLabel.setNext(falseBranch.getBeginning());
    	falseBranch.getEnd().setNext(endLabel);
    	BiTerminalGraph falseTarget = new BiTerminalGraph(falseLabel, endLabel);
    	
        return new BranchGraphFactory(
                new NativeExprGraphFactory(operation.getCondition(), scope).getGraph(),
                trueTarget,
                falseTarget)
                .getGraph();
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
