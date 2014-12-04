package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class DataFlowAnalyzer<T> {
    public static final DataFlowAnalyzer<ScopedExpression> AVAILABLE_EXPRESSIONS =
            new DataFlowAnalyzer<ScopedExpression>(new AvailabilitySpec());
    private AnalysisSpec<T> spec;

    public DataFlowAnalyzer(AnalysisSpec<T> spec) {
        this.spec = spec;
        //        calculateAvailability(entryBlock);
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
        Multimap<Node<ScopedStatement>, T> genSets = spec.getGenSets(savableNodes);
        Multimap<Node<ScopedStatement>, T> killSets = spec.getKillSets(savableNodes);
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

            newOut = spec.applyTransferFunction(genSets.get(node), inSets.get(node), killSets.get(node));

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
