package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.DataFlow;
import edu.mit.compilers.optimization.CommonExpressionEliminator;
import edu.mit.compilers.optimization.DataFlowOptimizer;

/**
 * Converts AST method nodes to control flow graphs.
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

    public BiTerminalGraph convert(Method method) {
        DataFlow dataFlowGraph = new BlockDataFlowFactory(method.getBlock()).getDataFlow();
        
        for (String optName : enabledOptimizations) {
            // TODO(jasonpr): Pass the whole dataFlowGraph, once the DataFlowOptimizer
            // interface is fixed.  (Right now, things will break if the beginnign node of
            // the dataFlowGraph is replaced as part of an optimization.
            OPTIMIZERS.get(optName).optimize(dataFlowGraph.getBeginning());
        }
        return new MethodGraphFactory(dataFlowGraph, method.getName(),
                method.isVoid(), method.getBlock().getMemorySize()).getGraph();
    }
}
