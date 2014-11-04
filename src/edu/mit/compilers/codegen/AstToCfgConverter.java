package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.DataFlow;

/**
 * Converts AST method nodes to control flow graphs.
 *
 * Eventually, this will be the place where we optionally apply optimizations.
 */
public class AstToCfgConverter {

    private AstToCfgConverter() {}

    public static BiTerminalGraph convert(Method method) {
        DataFlow dataFlowGraph = new BlockDataFlowFactory(method.getBlock()).getDataFlow();
        return new MethodGraphFactory(dataFlowGraph, method.getName(),
                method.isVoid(), method.getBlock().getMemorySize()).getGraph();
    }
}
