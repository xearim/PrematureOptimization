package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.CommonExpressionEliminator;
import edu.mit.compilers.optimization.DataFlowOptimizer;

/**
 * Converts AST method nodes to instruction-level control flow graphs.
 *
 * Eventually, this will be the place where we optionally apply optimizations.
 */
public class AstToCfgConverter {

    private static final Map<String, DataFlowOptimizer> OPTIMIZERS =
            ImmutableMap.<String, DataFlowOptimizer>of(
                    "cse", new CommonExpressionEliminator());

    private final Set<String> enabledOptimizations;
    
    // TODO(jasonpr): Improve the interface!  This set of strings is ugly.
    // (It's an artifact of the strange interface that tools.CLI provides...
    // but we could easily do a better job of isolating that strangeness.
    private AstToCfgConverter(Set<String> enabledOptimizations) {
        for (String optimization : enabledOptimizations) {
            checkArgument(OPTIMIZERS.containsKey(optimization));
        }
        this.enabledOptimizations = ImmutableSet.copyOf(enabledOptimizations);
    }
    
    public static AstToCfgConverter unoptimizing() {
        return new AstToCfgConverter(ImmutableSet.<String>of());
    }
    
    public static AstToCfgConverter withOptimizations(Set<String> optimizations) {
        return new AstToCfgConverter(optimizations);
    }

    public FlowGraph<Instruction> convert(Method method) {
        BcrFlowGraph<ScopedStatement> dataFlowGraph =
                new BlockDataFlowFactory(method.getBlock()).getDataFlow().asDataFlowGraph();
        DataFlowIntRep ir = new DataFlowIntRep(dataFlowGraph, method.getBlock().getScope());
        for (String optName : enabledOptimizations) {
            OPTIMIZERS.get(optName).optimize(ir);
        }
        return new MethodGraphFactory(dataFlowGraph, method.getName(),
                method.isVoid(), method.getBlock().getMemorySize()).getGraph();
    }
    
    public DataFlowIntRep optimize(Method method) {
        FlowGraph<ScopedStatement> dataFlowGraph =
                new BlockDataFlowFactory(method.getBlock()).getDataFlow().asDataFlowGraph();
        DataFlowIntRep ir = new DataFlowIntRep(dataFlowGraph, method.getBlock().getScope());
        for (String optName : enabledOptimizations) {
            OPTIMIZERS.get(optName).optimize(ir);
        }
        return ir;
    }
}
