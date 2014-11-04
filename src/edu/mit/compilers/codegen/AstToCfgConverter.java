package edu.mit.compilers.codegen;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.DataFlow;

public class AstToCfgConverter {
    public static BiTerminalGraph convert(Method method) {
        DataFlow dataFlowGraph = new BlockDataFlowFactory(method.getBlock()).getDataFlow();
        return new MethodGraphFactory(dataFlowGraph, method.getName(), method.isVoid()).getGraph();
    }
}
