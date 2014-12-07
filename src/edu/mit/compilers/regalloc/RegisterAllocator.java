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
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Graph;
import edu.mit.compilers.graph.Graphs;
import edu.mit.compilers.graph.Graphs.UncolorableGraphException;
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
            nodes.addAll(Graphs.semiOpenRange(dfg, duChain.getDef(), duChain.getUse()));
        }
        return new LiveRange(variable, nodes.build());
    }

    /** Map each live range to a register that can hold its variable's values. */
    private static Map<LiveRange, Register> getAllocations(
            Graph<LiveRange> conflictGraph) {

        Map<Node<LiveRange>, Register> coloredGraph;
        try {
            coloredGraph = Graphs.colored(conflictGraph, REGISTERS);
        } catch (UncolorableGraphException e) {
            // TODO(jasonpr): Handle uncolorable graphs.
            throw new RuntimeException("Not yet implemented!");
        }

        ImmutableMap.Builder<LiveRange, Register> allocations = ImmutableMap.builder();
        for (Node<LiveRange> node : coloredGraph.keySet()) {
            allocations.put(node.value(), coloredGraph.get(node));
        }
        return allocations.build();
    }
}
