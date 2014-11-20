package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     *
     * TODO(Manny): refactor so it's not one mega function.
     */
    public Map<DataFlowNode, Set<T>> calculateAvailability(DataFlowNode entryNode) {
        // Set up IN
        Map<DataFlowNode, Set<T>> inSets = createInSets(entryNode);
        Set<DataFlowNode> allBlocks = ImmutableSet.copyOf(inSets.keySet());

        Set<StatementDataFlowNode> savableNodes = getNodesWithSavableExpressions(allBlocks);
        Set<T> infinum = spec.getOriginalOutSet(savableNodes);
        Multimap<DataFlowNode, T> genSets = spec.getGenSets(savableNodes);
        Multimap<DataFlowNode, T> killSets = spec.getKillSets(savableNodes);

        // Set up OUT
        Map<DataFlowNode, Set<T>> outSets =
                new HashMap<DataFlowNode, Set<T>>();
        Set<DataFlowNode> changed;

        // Run algorithm
        for (DataFlowNode node : inSets.keySet()) {
            outSets.put(node, new HashSet<T>(infinum));
        }

        outSets.put(entryNode, new HashSet<T>(
                genSets.get(entryNode)));

        changed = new HashSet<DataFlowNode>(inSets.keySet());
        checkState(changed.remove(entryNode),
                "entryNode is not in set of all nodes.");

        while (!changed.isEmpty()) {
            Collection<Set<T>> predecessorOutSets = new ArrayList<Set<T>>();
            DataFlowNode node = changed.iterator().next();
            Set<T> newOut;
            changed.remove(node);
            
            for (DataFlowNode predecessor: node.getPredecessors()) {
                predecessorOutSets.add(outSets.get(predecessor));
            }
            inSets.put(node, spec.getInSet(predecessorOutSets, infinum));

            newOut = spec.getOutSetFromInSet(genSets.get(node), inSets.get(node), killSets.get(node));

            if (!newOut.equals(outSets.get(node))) {
                outSets.put(node, newOut);
                changed.addAll(node.getSuccessors());
            }
        }

        return inSets;
    }

    /**
     * Creates IN sets for all nodes. Does not initialize any values. Makes
     * sure that each node is included only once.
     */
    private Map<DataFlowNode, Set<T>> createInSets(DataFlowNode entryNode) {
        // Just do DFS. Use inSets as the visted set!
        Map<DataFlowNode, Set<T>> inSets =  new HashMap<DataFlowNode, Set<T>>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(entryNode);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (inSets.containsKey(node)) {
                continue;
            }

            // Add a new, currently empty, set of subexpressions.
            inSets.put(node, new HashSet<T>());

            for (DataFlowNode child : node.getSuccessors()) {
                queue.push(child);
            }
            for (DataFlowNode child : node.getPredecessors()) {
                queue.push(child);
            }
        }

        return inSets;
    }

    /*
     * This could've been beautiful - Jason - Manny
     */
    private static Set<StatementDataFlowNode> getNodesWithSavableExpressions(Iterable<DataFlowNode> allNodes) {
        ImmutableSet.Builder<StatementDataFlowNode> builder = ImmutableSet.builder();

        for (DataFlowNode node : allNodes) {
            if (!(node instanceof StatementDataFlowNode)) {
                continue;
            }

            StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
            if (!(statementNode.getExpression().isPresent())) {
                continue;
            }

            builder.add(statementNode);
        }

        return builder.build();
    }
}
