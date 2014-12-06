package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;
import static edu.mit.compilers.common.SetOperators.difference;
import static edu.mit.compilers.common.SetOperators.union;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class DataFlowAnalyzer<N, T> {
    public static final DataFlowAnalyzer<ScopedStatement, ReachingDefinition>
            REACHING_DEFINITIONS =
            new DataFlowAnalyzer<ScopedStatement, ReachingDefinition>(
                    new ReachingDefSpec());
    public static final DataFlowAnalyzer<ScopedStatement, ScopedExpression>
            AVAILABLE_EXPRESSIONS =
            new DataFlowAnalyzer<ScopedStatement, ScopedExpression>(new AvailabilitySpec());
    private AnalysisSpec<N, T> spec;

    public DataFlowAnalyzer(AnalysisSpec<N, T> spec) {
        this.spec = spec;
        //        calculateAvailability(entryBlock);
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     */
    public Multimap<Node<N>, T>
            calculate(FlowGraph<N> dataFlowGraph) {
        Set<Node<N>> allNodes = allNodes(dataFlowGraph);
        Set<Node<N>> savableNodes = spec.filterNodes(allNodes);
        Multimap<Node<N>, T> inSets = HashMultimap.<Node<N>,T>create();
        Multimap<Node<N>, T> outSets = HashMultimap.<Node<N>,T>create();
        Multimap<Node<N>, T> genSets = HashMultimap.<Node<N>,T>create(); 
        for (Node<N> node : savableNodes) {
            genSets.putAll(node, spec.getGenSet(node));
        }
        Set<Node<N>> changed;

        Node<N> entryNode = dataFlowGraph.getStart();
        // Run algorithm
        outSets.replaceValues(entryNode, genSets.get(entryNode));

        changed = new HashSet<Node<N>>(allNodes);
        checkState(changed.remove(entryNode),
                "entryNode is not in set of all nodes.");

        while (!changed.isEmpty()) {
            Node<N> node = changed.iterator().next();
            Set<T> newOut;
            changed.remove(node);

            Collection<Collection<T>> predecessorOutSets = new ArrayList<Collection<T>>();
            for (Node<N> predecessor: dataFlowGraph.getPredecessors(node)) {
                predecessorOutSets.add(outSets.get(predecessor));
            }
            inSets.replaceValues(node, spec.applyConfluenceOperator(predecessorOutSets));

            Collection<T> inSet = inSets.get(node);
            if (spec.gensImmuneToKills()) {
                Set<T> toKill = Sets.newHashSet();
                for (T candidate : inSet) {
                    if (spec.mustKill(node, candidate)) {
                        toKill.add(candidate);
                    }
                }
                newOut = union(spec.getGenSet(node), difference(inSet, toKill));
            } else {
                Set<T> inAndGen = union(spec.getGenSet(node), inSet);
                Set<T> toKill = Sets.newHashSet();
                for (T candidate : inAndGen) {
                    if (spec.mustKill(node, candidate)) {
                        toKill.add(candidate);
                    }
                }
                newOut = difference(inAndGen, toKill);
            }

            if (!newOut.equals(outSets.get(node))) {
                outSets.replaceValues(node, newOut);
                changed.addAll(dataFlowGraph.getSuccessors(node));
            }
        }

        return inSets;
    }

    // TODO(jasonpr): Consider moving this to some FlowGraphs class.
    /** Get all nodes in a graph that have a value. */
    private static <E> Set<Node<E>> allNodes(FlowGraph<E> dataFlowGraph) {
        return ImmutableSet.copyOf(dataFlowGraph.getNodes());
    }
}
