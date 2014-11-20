package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class DataFlowAnalyzer<T> {
    public static final DataFlowAnalyzer<Subexpression> AVAILABLE_EXPRESSIONS =
            new DataFlowAnalyzer<Subexpression>(new AvailabilitySpec());
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
    public Multimap<DataFlowNode, T> calculateAvailability(DataFlowNode entryNode) {
        Set<DataFlowNode> allNodes = getAllNodes(entryNode);
        Set<StatementDataFlowNode> savableNodes = spec.filterNodes(allNodes);
        Set<T> infinum = spec.getInfinum(savableNodes);
        Multimap<DataFlowNode, T> inSets = HashMultimap.<DataFlowNode,T>create();
        Multimap<DataFlowNode, T> outSets = HashMultimap.<DataFlowNode,T>create();
        Multimap<DataFlowNode, T> genSets = spec.getGenSets(savableNodes);
        Multimap<DataFlowNode, T> killSets = spec.getKillSets(savableNodes);
        Set<DataFlowNode> changed;

        // Run algorithm
        outSets.replaceValues(entryNode, genSets.get(entryNode));

        changed = new HashSet<DataFlowNode>(allNodes);
        checkState(changed.remove(entryNode),
                "entryNode is not in set of all nodes.");

        while (!changed.isEmpty()) {
            DataFlowNode node = changed.iterator().next();
            Set<T> newOut;
            changed.remove(node);

            Collection<Collection<T>> predecessorOutSets = new ArrayList<Collection<T>>();
            for (DataFlowNode predecessor: node.getPredecessors()) {
                predecessorOutSets.add(outSets.get(predecessor));
            }
            inSets.replaceValues(node, spec.getInSetFromPredecessors(predecessorOutSets, infinum));

            newOut = spec.getOutSetFromInSet(genSets.get(node), inSets.get(node), killSets.get(node));

            if (!newOut.equals(outSets.get(node))) {
                outSets.replaceValues(node, newOut);
                changed.addAll(node.getSuccessors());
            }
        }

        return inSets;
    }

    /** Uses DFS to retrieve all nodes from the entryNode. */
    private Set<DataFlowNode> getAllNodes(DataFlowNode entryNode) {
        // Just do DFS.
        Set<DataFlowNode> visited = new HashSet<DataFlowNode>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(entryNode);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (visited.contains(node)) {
                continue;
            }

            // Add a new, currently empty, set of subexpressions.
            visited.add(node);

            for (DataFlowNode child : node.getSuccessors()) {
                queue.push(child);
            }
            for (DataFlowNode child : node.getPredecessors()) {
                queue.push(child);
            }
        }

        return ImmutableSet.copyOf(visited);
    }
}
