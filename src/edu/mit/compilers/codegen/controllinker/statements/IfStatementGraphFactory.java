package edu.mit.compilers.codegen.controllinker.statements;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.BlockGraphFactory;
import edu.mit.compilers.codegen.controllinker.BranchGraphFactory;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class IfStatementGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public IfStatementGraphFactory(IfStatement ifStatement, Scope scope) {
        this.graph = calculateGraph(ifStatement, scope);
    }

    private ControlTerminalGraph calculateGraph(IfStatement ifStatement,
            Scope scope) {
        // Conditional
        BiTerminalGraph conditionalGraph = new NativeExprGraphFactory(
                ifStatement.getCondition(), scope).getGraph();

        // Obtain then block
        ControlTerminalGraph thenBlockGraph =
                new BlockGraphFactory(ifStatement.getThenBlock(),
                        scope).getGraph();

        // Obtain else block
        Optional<Block> elseBlock = ifStatement.getElseBlock();
        ControlTerminalGraph elseBlockGraph;
        if (elseBlock.isPresent()) {
            elseBlockGraph =
                    new BlockGraphFactory(elseBlock.get(), scope).getGraph();
        } else {
            elseBlockGraph = ControlTerminalGraph.nopTerminal();
        }

        ControlTerminalGraph ifGraph = new BranchControlGraphFactory(conditionalGraph,
                thenBlockGraph, elseBlockGraph).getGraph();

        /*
         * TODO(xearim):
         * make sure this is the scheme we want to use,
         * if it is, then remove the exception, uncomment the return,
         * and implement BranchControlGraphFactory
         */

        throw new RuntimeException("IfStatementGraphFactory not implemented yet");

        // return ifGraph;
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
