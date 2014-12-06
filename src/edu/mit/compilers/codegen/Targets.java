package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.CommonExpressionEliminator;
import edu.mit.compilers.optimization.ConstantPropagator;
import edu.mit.compilers.optimization.DataFlowOptimizer;
import edu.mit.compilers.optimization.DeadCodeEliminator;

/** Executes major, high-level compilation steps. */
public class Targets {

    private static final Map<String, DataFlowOptimizer> OPTIMIZERS =
            ImmutableMap.<String, DataFlowOptimizer>of(
                    "cse", new CommonExpressionEliminator(),
                    "conprop", new ConstantPropagator(),
                    "dce", new DeadCodeEliminator());

    public static DataFlowIntRep unoptimizedDataFlowIntRep(Method method) {
        return asDataFlowIntRep(method);
    }

    public static DataFlowIntRep
            optimizedDataFlowIntRep(Method method, Set<String> dataflowOptimizations) {
        return optimized(unoptimizedDataFlowIntRep(method), dataflowOptimizations);
    }

    public static FlowGraph<Instruction>
            controlFlowGraph(Method method, Set<String> dataflowOptimizations) {
        return asControlFlowGraph(optimizedDataFlowIntRep(method, dataflowOptimizations),
                method.getName(), method.isVoid(), method.getBlock().getMemorySize());
    }

    private static DataFlowIntRep asDataFlowIntRep(Method method) {
        BcrFlowGraph<ScopedStatement> dataFlowGraph =
                new BlockDataFlowFactory(method.getBlock()).getDataFlow().asDataFlowGraph();
        return new DataFlowIntRep(dataFlowGraph, method.getBlock().getScope());
    }

    // TODO(jasonpr): Improve the interface!  This set of strings is ugly.
    // (It's an artifact of the strange interface that tools.CLI provides...
    // but we could easily do a better job of isolating that strangeness.
    private static DataFlowIntRep optimized(DataFlowIntRep unoptimized, Set<String> enabledOptimizations) {
        for (String optimization : enabledOptimizations) {
            checkArgument(OPTIMIZERS.containsKey(optimization));
        }

        DataFlowIntRep ir = unoptimized;
        // Do dataflow optimizations.
        for (String optName : enabledOptimizations) {
            ir = OPTIMIZERS.get(optName).optimized(ir);
        }
        // Remove NOPs, for easy printing.
        BcrFlowGraph<ScopedStatement> dfg = BcrFlowGraph.builderOf(ir.getDataFlowGraph())
                .removeNops()
                .build();
        return new DataFlowIntRep(dfg, ir.getScope());

    }

    private static FlowGraph<Instruction> asControlFlowGraph(
            DataFlowIntRep ir, String name, boolean isVoid,  long memorySize) {
        FlowGraph<Instruction> cfg = new MethodGraphFactory(
                ir.getDataFlowGraph(),name, isVoid, memorySize).getGraph();
        return BasicFlowGraph.builderOf(cfg).removeNops().build();
    }
}
