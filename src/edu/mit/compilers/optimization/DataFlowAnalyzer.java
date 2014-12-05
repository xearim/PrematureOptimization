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
public class DataFlowAnalyzer<T> {
    public static final DataFlowAnalyzer<ReachingDefinition> REACHING_DEFINITIONS =
            new DataFlowAnalyzer<ReachingDefinition>(new ReachingDefSpec());
    public static final DataFlowAnalyzer<ScopedExpression> AVAILABLE_EXPRESSIONS =
            new DataFlowAnalyzer<ScopedExpression>(new AvailabilitySpec());
    private AnalysisSpec<T> spec;

    public DataFlowAnalyzer(AnalysisSpec<T> spec) {
        this.spec = spec;
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     */
    public Multimap<Node<ScopedStatement>, T>
        calculateAvailability(FlowGraph<ScopedStatement> dataFlowGraph) {

        Set<Node<ScopedStatement>> allNodes = allNodes(dataFlowGraph);
        Set<Node<ScopedStatement>> savableNodes = spec.filterNodes(allNodes);
        Multimap<Node<ScopedStatement>, T> inSets = HashMultimap.<Node<ScopedStatement>,T>create();
        Multimap<Node<ScopedStatement>, T> outSets = HashMultimap.<Node<ScopedStatement>,T>create();
        Multimap<Node<ScopedStatement>, T> genSets = HashMultimap.<Node<ScopedStatement>,T>create(); 
        for (Node<ScopedStatement> node : savableNodes) {
            genSets.putAll(node, spec.getGenSet(node));
        }
        Set<Node<ScopedStatement>> changed;

        Node<ScopedStatement> entryNode = dataFlowGraph.getStart();
        // Run algorithm
        outSets.replaceValues(entryNode, genSets.get(entryNode));

        changed = new HashSet<Node<ScopedStatement>>(allNodes);
        checkState(changed.remove(entryNode),
                "entryNode is not in set of all nodes.");

        while (!changed.isEmpty()) {
            Node<ScopedStatement> node = changed.iterator().next();
            Set<T> newOut;
            changed.remove(node);

            Collection<Collection<T>> predecessorOutSets = new ArrayList<Collection<T>>();
            for (Node<ScopedStatement> predecessor: dataFlowGraph.getPredecessors(node)) {
                predecessorOutSets.add(outSets.get(predecessor));
            }
            inSets.replaceValues(node, spec.applyConfluenceOperator(predecessorOutSets));

            newOut = calculateNewOut(node, inSets.get(node));

            if (!newOut.equals(outSets.get(node))) {
                outSets.replaceValues(node, newOut);
                changed.addAll(dataFlowGraph.getSuccessors(node));
            }
        }

        return inSets;
    }

    /**
     * Depending on the AnalysisSpec#gensImmuneToKills, applies one of the following:
     * true) GEN U (IN - KILL)
     * false) (GEN U IN) - KILL
     */
    private Set<T> calculateNewOut(Node<ScopedStatement> node,
            Collection<T> inSet) {

        if (spec.gensImmuneToKills()) {
            Set<T> toKill = Sets.newHashSet();
            for (T candidate : inSet) {
                if (spec.mustKill(node, candidate)) {
                    toKill.add(candidate);
                }
            }
            return union(spec.getGenSet(node), difference(inSet, toKill));
        } else {
            Set<T> inAndGen = union(spec.getGenSet(node), inSet);
            Set<T> toKill = Sets.newHashSet();
            for (T candidate : inAndGen) {
                if (spec.mustKill(node, candidate)) {
                    toKill.add(candidate);
                }
            }
            return difference(inAndGen, toKill);
        }
    }

    // TODO(jasonpr): Consider moving this to some FlowGraphs class.
    /** Get all nodes in a graph that have a value. */
    private static <E> Set<Node<E>> allNodes(FlowGraph<E> dataFlowGraph) {
        return ImmutableSet.copyOf(dataFlowGraph.getNodes());
    }
}
