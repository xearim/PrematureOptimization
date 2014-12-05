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
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     */
    public Multimap<Node<N>, T>
            calculateAvailability(FlowGraph<N> dataFlowGraph) {
        Set<Node<N>> allNodes = allNodes(dataFlowGraph);
        Set<Node<N>> savableNodes = spec.filterNodes(allNodes);
        Multimap<Node<N>, T> inputSets = HashMultimap.<Node<N>,T>create();
        Multimap<Node<N>, T> outputSets = HashMultimap.<Node<N>,T>create();
        Multimap<Node<N>, T> genSets = HashMultimap.<Node<N>,T>create(); 
        for (Node<N> node : savableNodes) {
            genSets.putAll(node, spec.getGenSet(node));
        }
        Set<Node<N>> changed;

        Node<N> entryNode = getEntryNode(dataFlowGraph);
        // Run algorithm
        outputSets.replaceValues(entryNode, genSets.get(entryNode));

        changed = new HashSet<Node<N>>(allNodes);
        checkState(changed.remove(entryNode),
                "entryNode is not in set of all nodes.");

        while (!changed.isEmpty()) {
            Node<N> node = changed.iterator().next();
            Set<T> newOutput;
            changed.remove(node);

            Collection<Collection<T>> sourceOutputSets = new ArrayList<Collection<T>>();
            for (Node<N> source: getSources(dataFlowGraph, node)) {
                sourceOutputSets.add(outputSets.get(source));
            }
            inputSets.replaceValues(node, spec.applyConfluenceOperator(sourceOutputSets));

            newOutput = calculateNewOutput(node, inputSets.get(node));

            if (!newOutput.equals(outputSets.get(node))) {
                outputSets.replaceValues(node, newOutput);
                changed.addAll(dataFlowGraph.getSuccessors(node));
            }
        }

        return inputSets;
    }

    private Node<N> getEntryNode(
            FlowGraph<N> dataFlowGraph) {

        if (spec.isForward()) {
            return dataFlowGraph.getStart();
        } else {
            return getExitNode(dataFlowGraph);
        }
    }

    private Node<N> getExitNode(
            FlowGraph<N> dataFlowGraph) {
        throw new UnsupportedOperationException("unimplemented");
    }

    private Set<Node<N>> getSources(
            FlowGraph<N> dataFlowGraph, Node<N> node) {

        if (spec.isForward()){
            return dataFlowGraph.getPredecessors(node);
        } else {
            return dataFlowGraph.getSuccessors(node);
        }
    }

    /**
     * Depending on the AnalysisSpec#gensImmuneToKills, applies one of the following:
     * true) GEN U (IN - KILL)
     * false) (GEN U IN) - KILL
     */
    private Set<T> calculateNewOutput(Node<N> node,
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
