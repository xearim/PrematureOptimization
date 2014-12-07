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
import edu.mit.compilers.common.SetOperators;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.loops.DominatorSpec;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class DataFlowAnalyzer<N, T> {
    public static final DataFlowAnalyzer<ScopedStatement, ReachingDefinition>
            REACHING_DEFINITIONS =
            new DataFlowAnalyzer<ScopedStatement, ReachingDefinition>(new ReachingDefSpec());
    public static final DataFlowAnalyzer<ScopedStatement, ScopedExpression>
            AVAILABLE_EXPRESSIONS =
            new DataFlowAnalyzer<ScopedStatement, ScopedExpression>(new AvailabilitySpec());
    public static final DataFlowAnalyzer<ScopedStatement, ScopedVariable>
            LIVE_VARIABLES =
            new DataFlowAnalyzer<ScopedStatement, ScopedVariable>(new LivenessSpec());
    public static final DataFlowAnalyzer<ScopedStatement, Node<ScopedStatement>>
            DOMINATORS =
            new DataFlowAnalyzer<ScopedStatement, Node<ScopedStatement>>(
                    new DominatorSpec<ScopedStatement>());

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
            calculate(BcrFlowGraph<N> dataFlowGraph) {
        Set<Node<N>> allNodes = allNodes(dataFlowGraph);
        Multimap<Node<N>, T> inputSets = HashMultimap.<Node<N>,T>create();
        Multimap<Node<N>, T> outputSets = HashMultimap.<Node<N>,T>create();
        Set<Node<N>> changed;

        changed = new HashSet<Node<N>>(allNodes);
        for (Node<N> entryNode : getEntryNodes(dataFlowGraph)) {
            outputSets.replaceValues(entryNode, spec.getGenSet(entryNode, ImmutableSet.<T>of()));
        checkState(changed.remove(entryNode),
                "Entry node %s is not in set of all nodes.", entryNode);
        }

        while (!changed.isEmpty()) {
            Node<N> node = changed.iterator().next();
            Collection<T> oldInput;
            Set<T> newOutput;
            changed.remove(node);

            oldInput = inputSets.get(node);
            Collection<Collection<T>> sourceOutputSets = new ArrayList<Collection<T>>();
            Set<Node<N>> sources = getSources(dataFlowGraph, node, inputSets);

            for (Node<N> source: sources) {
                sourceOutputSets.add(outputSets.get(source));
            }
            inputSets.replaceValues(node, spec.applyConfluenceOperator(sourceOutputSets));

            if (spec instanceof DominatorSpec) {
                for (T newInputNode : SetOperators.difference(inputSets.get(node), oldInput)) {
                    // In the case of DominatorSpec, it is true that Node<N> and T are equivalent.
                    @SuppressWarnings("unchecked")
                    Node<N> recastNewInputNode = (Node<N>) newInputNode;
                    if (dataFlowGraph.getSuccessors(node).contains(recastNewInputNode)) {
                        changed.add(recastNewInputNode);
                    }
                }
            }

            newOutput = calculateNewOutput(node, inputSets.get(node));

            if (!newOutput.equals(outputSets.get(node))) {
                outputSets.replaceValues(node, newOutput);
                changed.addAll(getSinks(dataFlowGraph, node));
            }
        }

        return inputSets;
    }

    private Collection<Node<N>> getEntryNodes(
            BcrFlowGraph<N> dataFlowGraph) {
        if (spec.isForward()) {
            return ImmutableSet.of(dataFlowGraph.getStart());
        } else {
            return getExitNodes(dataFlowGraph);
        }
    }

    private Collection<Node<N>> getExitNodes(BcrFlowGraph<N> dataFlowGraph) {
        return ImmutableSet.of(
                dataFlowGraph.getReturnTerminal(),
                dataFlowGraph.getEnd());
    }

    /**
     * Returns the successors or predecessors depending on whether the
     * DataFlowAnalyzer is forward or backward propagating.
     *
     * There is a special case for calculating dominators. The sources need to
     * be nodes that aren't dominated.
     */
    private Set<Node<N>> getSources(
            FlowGraph<N> dataFlowGraph, Node<N> node, Multimap<Node<N>, T> inputSets) {
        Set<Node<N>> sources =
                spec.isForward()
                ? dataFlowGraph.getPredecessors(node)
                : dataFlowGraph.getSuccessors(node);

        if (spec instanceof DominatorSpec) {
            // Filter out all back edges
            ImmutableSet.Builder<Node<N>> sourcesBuilder = ImmutableSet.builder();
            for (Node<N> candidate : sources) {
                if (!inputSets.get(candidate).contains(node)) {
                    sourcesBuilder.add(candidate);
                }
            }
            sources = sourcesBuilder.build();
        }

        return sources;
    }

    private Set<Node<N>> getSinks(
            FlowGraph<N> dataFlowGraph, Node<N> node) {
        return spec.isForward()
                ? dataFlowGraph.getSuccessors(node)
                : dataFlowGraph.getPredecessors(node);
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
            return union(spec.getGenSet(node, inSet), difference(inSet, toKill));
        } else {
            Set<T> inAndGen = union(spec.getGenSet(node, inSet), inSet);
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
