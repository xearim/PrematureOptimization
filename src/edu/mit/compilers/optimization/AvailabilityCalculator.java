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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;

/**
 * Given a basic block, computes all available Ts at each block accessible from
 * the input basic block.
 */
public class AvailabilityCalculator<T> {
    public static final AvailabilityCalculator<Subexpression> AVAILABLE_EXPRESSIONS = new AvailabilityCalculator<Subexpression>(new AvailabilitySpec());
    private Multimap<DataFlowNode, T> inSets;
    private AnalysisSpec<T> spec;

    public AvailabilityCalculator (AnalysisSpec<T> spec) {
        this.spec = spec;
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     */
    public Multimap<DataFlowNode, T> calculate(DataFlowNode entryNode) {
        // Set up IN
        Multimap<DataFlowNode, T> inSets = createInSets(entryNode);
        Set<DataFlowNode> allNodes = ImmutableSet.copyOf(inSets.keySet());

        Set<StatementDataFlowNode> savableNodes = getNodesWithSavableExpressions(allNodes);
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
                "entryBlock is not in set of all blocks.");

        while (!changed.isEmpty()) {
            DataFlowNode node;
            Set<T> newOut;

            node = changed.iterator().next();
            changed.remove(node);

            Collection<Set<T>> allOutSets = new ArrayList<Set<T>>();
            for (DataFlowNode predecessor: node.getPredecessors()) {
                allOutSets.add(outSets.get(predecessor));
            }
            inSets.replaceValues(node, spec.getInSet(allOutSets, infinum));

            newOut = spec.getOutSetFromInSet(genSets.get(node), inSets.get(node), killSets.get(node));

            if (!newOut.equals(outSets.get(node))) {
                outSets.put(node, newOut);
                changed.addAll(node.getSuccessors());
            }
        }
        
        return inSets;
    }

    /**
     * Creates IN sets for all blocks. Does not initialize any values. Makes
     * sure that each block is included only once.
     */
    private Multimap<DataFlowNode, T> createInSets(DataFlowNode entryBlock) {
        // Just do DFS.
        Multimap<DataFlowNode, T> inSets = HashMultimap.create();
        Set<DataFlowNode> visited = new HashSet<DataFlowNode>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(entryBlock);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (visited.contains(node)) {
                continue;
            }

            visited.add(node);
            
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
