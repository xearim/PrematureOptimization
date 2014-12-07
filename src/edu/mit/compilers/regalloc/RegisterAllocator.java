package edu.mit.compilers.regalloc;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.Register.R8;
import static edu.mit.compilers.codegen.asm.Register.R9;
import static edu.mit.compilers.codegen.asm.Register.RAX;
import static edu.mit.compilers.codegen.asm.Register.RCX;
import static edu.mit.compilers.codegen.asm.Register.RDI;
import static edu.mit.compilers.codegen.asm.Register.RDX;
import static edu.mit.compilers.codegen.asm.Register.RSI;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Graph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.DataFlowAnalyzer;
import edu.mit.compilers.optimization.ReachingDefinition;
import edu.mit.compilers.optimization.ScopedVariable;

public class RegisterAllocator {

    /** The registers we can allocate. */
    private static final Set<Register> REGISTERS =
            ImmutableSet.of(RAX, RCX, RDX, RSI, RDI, R8, R9, R10, R11);

    private RegisterAllocator() {}

    public static Map<LiveRange, Register> allocations(BcrFlowGraph<ScopedStatement> dfg) {
        Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs =
                DataFlowAnalyzer.REACHING_DEFINITIONS.calculate(dfg);
        Multimap<ScopedVariable, DefUseChain> defUseChains = DefUseChain.getDefUseChains(reachingDefs);
        Multimap<ScopedVariable, Web> webs = getWebs(defUseChains);
        Set<LiveRange> liveRanges = getLiveRanges(webs, dfg);
        Graph<LiveRange> conflictGraph = getConflictGraph(liveRanges);
        return getAllocations(conflictGraph);
    }

    /** For each key, map the set of def-use chains to a (probably smaller) set of webs. */
    private static Multimap<ScopedVariable, Web> getWebs(
            Multimap<ScopedVariable, DefUseChain> defUseChains) {
        ImmutableMultimap.Builder<ScopedVariable, Web> websBuilder = ImmutableMultimap.builder();
        for (ScopedVariable variable : defUseChains.keySet()) {
            websBuilder.putAll(variable, Web.webs(defUseChains.get(variable)));
        }
        return websBuilder.build();
    }

    private static Set<LiveRange> getLiveRanges(
            Multimap<ScopedVariable, Web> webs,
            BcrFlowGraph<ScopedStatement> dfg) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Graph<LiveRange> getConflictGraph(Set<LiveRange> liveRanges) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Map<LiveRange, Register> getAllocations(
            Graph<LiveRange> conflictGraph) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
