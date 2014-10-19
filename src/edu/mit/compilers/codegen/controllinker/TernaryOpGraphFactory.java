package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.codegen.SequentialControlFlowNode;

public class TernaryOpGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;
    
    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope) {
        this.graph = calculateGraph(operation, scope);
    }

    private BiTerminalGraph calculateGraph(TernaryOperation operation, Scope scope) {
    	
    	// Construct a label for the false branch
        SequentialControlFlowNode trueNode = SequentialControlFlowNode.namedNop("ternary true");
        SequentialControlFlowNode falseNode = SequentialControlFlowNode.namedNop("ternary false");
    	
    	BiTerminalGraph trueBranch = new NativeExprGraphFactory(operation.getTrueResult(), scope).getGraph();
    	BiTerminalGraph falseBranch = new NativeExprGraphFactory(operation.getFalseResult(), scope).getGraph();
    	
    	// Hook up the true target
        trueNode.setNext(trueBranch.getBeginning());
        BiTerminalGraph trueTarget = new BiTerminalGraph(trueNode,
                SequentialControlFlowNode.nopTerminal());
    	
    	// Hook up the false target
    	falseNode.setNext(falseBranch.getBeginning());
    	BiTerminalGraph falseTarget = new BiTerminalGraph(falseNode,
                SequentialControlFlowNode.nopTerminal());
        
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
