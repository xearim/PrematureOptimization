package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.MethodGraphFactory;
import edu.mit.compilers.codegen.dataflow.BlockDataFlowFactory;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.CommonExpressionEliminator;
import edu.mit.compilers.optimization.ConstantPropagator;
import edu.mit.compilers.optimization.DataFlowOptimizer;
import edu.mit.compilers.optimization.DeadCodeEliminator;
import edu.mit.compilers.optimization.DominatorTreeGenerator;
import edu.mit.compilers.optimization.SubexpressionExpander;

/** Executes major, high-level compilation steps. */
public class Targets {
	
	private static final Map<String, DataFlowOptimizer> PREPROCESSING =
            ImmutableMap.<String, DataFlowOptimizer>of(
                    "sse", new SubexpressionExpander());

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
        
        // Do dataflow preprocessing
        for (String optName : PREPROCESSING.keySet()) {
            ir = PREPROCESSING.get(optName).optimized(ir);
        }
        
        // Do dataflow optimizations.
        for (String optName : enabledOptimizations) {
        	if(true){
        		ir = OPTIMIZERS.get(optName).optimized(ir);
        	}
        }
        return ir;

    }

    private static FlowGraph<Instruction> asControlFlowGraph(
            DataFlowIntRep ir, String name, boolean isVoid, long memorySize) {
        FlowGraph<Instruction> cfg = new MethodGraphFactory(
                ir.getDataFlowGraph(),name, isVoid, getMemorySize(ir, memorySize)).getGraph();
        return BasicFlowGraph.builderOf(cfg).removeNops().build();
    }
    
    private static long getMemorySize(DataFlowIntRep ir, long original){
    	long size = original;
    	for(Node<ScopedStatement> node : ir.getDataFlowGraph().getNodes()){
    		long nodeReq = 0;
    		if(node.hasValue()){
    			Scope current = node.value().getScope();
    			do{
    				nodeReq += current.getScopeType() == ScopeType.GLOBAL ? 0 : current.size();
    				current = current.getParent().isPresent() ? current.getParent().get() : null;
    			}while(current != null && !current.equals(ir.getScope()));
    		}
    		size = size < nodeReq ? nodeReq : size;
    	}
    	return size;
    }

    /** Returns a dominator tree for the data flow graph of a method */
    public static DiGraph<ScopedStatement> dominatorTree(Method method,
            Set<String> optimizationNames) {
        BcrFlowGraph<ScopedStatement> graph = optimizedDataFlowIntRep(method, optimizationNames).getDataFlowGraph();
        return DominatorTreeGenerator.<ScopedStatement>getDominatorTree(graph);
    }
}
