package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Register;
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
        SequentialControlFlowNode start = SequentialControlFlowNode.namedNop("RS start");
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("RS end");
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("RS cont");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("RS break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("RS return");
        Optional<NativeExpression> value = rs.getValue(); 

        if (value.isPresent()) {
            BiTerminalGraph putReturnValueInReturnRegister =
                    BiTerminalGraph.sequenceOf(
                            new NativeExprGraphFactory(value.get(), scope).getGraph(),
                            BiTerminalGraph.ofInstructions(pop(Register.RAX)));

            start.setNext(putReturnValueInReturnRegister.getBeginning());
            putReturnValueInReturnRegister.getEnd().setNext(returnNode);
        } else {
            // Don't modify the stack or any registers.
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
