package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.codegen.SequentialControlFlowNode;

public class TernaryOpGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;
    
    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope, boolean inMethodCall) {
        this.graph = calculateGraph(operation, scope, inMethodCall);
    }

    private BiTerminalGraph calculateGraph(TernaryOperation operation, Scope scope, boolean inMethodCall) {
    	
    	// Construct a label for the false branch
        SequentialControlFlowNode trueNode = SequentialControlFlowNode.namedNop("ternary true");
        SequentialControlFlowNode falseNode = SequentialControlFlowNode.namedNop("ternary false");
    	
    	BiTerminalGraph trueBranch = new NativeExprGraphFactory(operation.getTrueResult(), scope, inMethodCall).getGraph();
    	BiTerminalGraph falseBranch = new NativeExprGraphFactory(operation.getFalseResult(), scope, inMethodCall).getGraph();
    	
    	// Hook up the true target
        trueNode.setNext(trueBranch.getBeginning());
        BiTerminalGraph trueTarget = new BiTerminalGraph(trueNode,
                trueBranch.getEnd());
    	
    	// Hook up the false target
    	falseNode.setNext(falseBranch.getBeginning());
    	BiTerminalGraph falseTarget = new BiTerminalGraph(falseNode,
                falseBranch.getEnd());
        
        return new BranchGraphFactory(
                new NativeExprGraphFactory(operation.getCondition(), scope, inMethodCall).getGraph(),
                trueTarget,
                falseTarget)
                .getGraph();
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
