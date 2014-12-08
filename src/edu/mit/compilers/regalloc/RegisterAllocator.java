package edu.mit.compilers.regalloc;

import static edu.mit.compilers.codegen.asm.Register.R12;
import static edu.mit.compilers.codegen.asm.Register.R13;
import static edu.mit.compilers.codegen.asm.Register.R14;
import static edu.mit.compilers.codegen.asm.Register.R15;
import static edu.mit.compilers.codegen.asm.Register.RBX;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Graph;
import edu.mit.compilers.graph.Graphs;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.DataFlowAnalyzer;
import edu.mit.compilers.optimization.ReachingDefinition;
import edu.mit.compilers.optimization.ScopedVariable;

public class RegisterAllocator {

    /** The registers we can allocate. */
    public static final List<Register> REGISTERS =
            ImmutableList.of(R12, R13, R14, R15, RBX);

    private RegisterAllocator() {}

    public static Map<LiveRange, Register> allocations(BcrFlowGraph<ScopedStatement> dfg) {
        Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs =
                DataFlowAnalyzer.REACHING_DEFINITIONS.calculate(dfg);
        Multimap<ScopedVariable, DefUseChain> defUseChains = DefUseChain.getDefUseChains(reachingDefs);
        Multimap<ScopedVariable, Web> webs = getWebs(defUseChains);
        Set<LiveRange> liveRanges = getLiveRanges(webs, dfg);
        Graph<LiveRange> conflictGraph = LiveRange.conflictGraph(liveRanges);
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

    /** Gets the live ranges represented by some webs. */
    private static Set<LiveRange> getLiveRanges(
            Multimap<ScopedVariable, Web> webs,
            BcrFlowGraph<ScopedStatement> dfg) {
        ImmutableSet.Builder<LiveRange> liveRangesBuilder = ImmutableSet.builder();
        for (Entry<ScopedVariable, Web> entry: webs.entries()) {
            liveRangesBuilder.add(getLiveRange(entry.getKey(), entry.getValue(), dfg));
        }
        return liveRangesBuilder.build();
    }

    /**
     * Gets the LiveRange of a web.
     *
     * @param variable The variable whose liveness this web represents.
     * @param web A web of def-use chains for this variable.
     * @param dfg The Data Flow Graph, for determining which nodes are part of the live range.
     */
    private static LiveRange
            getLiveRange(ScopedVariable variable, Web web, BcrFlowGraph<ScopedStatement> dfg) {
        ImmutableSet.Builder<Node<ScopedStatement>> nodes = ImmutableSet.builder();
        for (DefUseChain duChain : web) {
            nodes.addAll(Graphs.closedRange(dfg, duChain.getDef(), duChain.getUse()));
        }
        return new LiveRange(variable, nodes.build());
    }

    /** Map each live range to a register that can hold its variable's values. */
    private static Map<LiveRange, Register> getAllocations(
            Graph<LiveRange> conflictGraph) {

        Map<Node<LiveRange>, Register> coloredGraph;
        coloredGraph = Graphs.colored(conflictGraph, ImmutableSet.copyOf(REGISTERS));

        ImmutableMap.Builder<LiveRange, Register> allocations = ImmutableMap.builder();
        for (Node<LiveRange> node : coloredGraph.keySet()) {
            allocations.put(node.value(), coloredGraph.get(node));
        }
        return allocations.build();
    }
}
