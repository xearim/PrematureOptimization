package edu.mit.compilers.codegen.controllinker.statements;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class ReturnStatementGraphFactory implements ControlTerminalGraphFactory {
    private ControlTerminalGraph graph;
    
    public ReturnStatementGraphFactory(ReturnStatement rs, Scope scope) {
        this.graph = calculateGraph(rs, scope);
    }
    
    private ControlTerminalGraph calculateGraph(ReturnStatement rs, Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal();
        Optional<NativeExpression> value = rs.getValue(); 

        if (value.isPresent()) {
            BiTerminalGraph valueGraph =
                    new NativeExprGraphFactory(value.get(), scope).getGraph();
            start.setNext(valueGraph.getBeginning());
            valueGraph.getEnd().setNext(returnNode);
        } else {
            start.setNext(returnNode);
        }
        
        return new ControlTerminalGraph(start,end,
                new ControlNodes(breakNode,continueNode,returnNode));
        
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
