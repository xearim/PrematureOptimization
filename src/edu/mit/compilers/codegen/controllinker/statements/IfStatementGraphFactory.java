package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.BlockGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class IfStatementGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public IfStatementGraphFactory(IfStatement ifStatement, Scope scope) {
        this.graph = calculateGraph(ifStatement, scope);
    }

    private ControlTerminalGraph calculateGraph(IfStatement ifStatement,
            Scope scope) {
        SequentialControlFlowNode start = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.nopTerminal();
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.nopTerminal(); 

        // Conditional
        BiTerminalGraph conditionalGraph = new NativeExprGraphFactory(
                ifStatement.getCondition(), scope).getGraph();
        start.setNext(conditionalGraph.getBeginning());
        
        BiTerminalGraph ifComparator =
                BiTerminalGraph.ofInstructions(
                        pop(Register.R10),
                        move(Literal.TRUE, Register.R11),
                        compareFlagged(Register.R11, Register.R10));
        conditionalGraph.getEnd().setNext(ifComparator.getBeginning());
        
        // Obtain then block
        ControlTerminalGraph thenBlockGraph =
                new BlockGraphFactory(ifStatement.getThenBlock()).getGraph();
        thenBlockGraph.getControlNodes().getBreakNode().setNext(breakNode);
        thenBlockGraph.getControlNodes().getContinueNode().setNext(continueNode);
        thenBlockGraph.getControlNodes().getReturnNode().setNext(returnNode);
        thenBlockGraph.getEnd().setNext(end);

        // Obtain else block
        Optional<Block> elseBlock = ifStatement.getElseBlock();
        ControlTerminalGraph elseBlockGraph = (elseBlock.isPresent())
                ? new BlockGraphFactory(elseBlock.get()).getGraph() 
                : ControlTerminalGraph.nopTerminal();
        elseBlockGraph.getControlNodes().getBreakNode().setNext(breakNode);
        elseBlockGraph.getControlNodes().getContinueNode().setNext(continueNode);
        elseBlockGraph.getControlNodes().getReturnNode().setNext(returnNode);
        elseBlockGraph.getEnd().setNext(end);

        BranchingControlFlowNode branch =
                new BranchingControlFlowNode(
                        JumpType.JNE,
                        thenBlockGraph.getBeginning(),
                        elseBlockGraph.getBeginning());
        ifComparator.getEnd().setNext(branch);
        
        return new ControlTerminalGraph(start, end,
                new ControlNodes(breakNode,continueNode,returnNode));
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
