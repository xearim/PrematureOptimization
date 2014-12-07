package edu.mit.compilers.regalloc;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Graph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.ScopedVariable;

/**
 * The set of DFG nodes over which a definition web of a variable is live.
 *
 * <p>Excludes the node at which the definition goes dead!
 */
public class LiveRange {
    private final ScopedVariable scopedVariable;
    private final Set<Node<ScopedStatement>> liveNodes;

    public LiveRange(ScopedVariable scopedVariable, Iterable<Node<ScopedStatement>> liveNodes) {
        this.scopedVariable = scopedVariable;
        this.liveNodes = ImmutableSet.copyOf(liveNodes);
    }

    public ScopedVariable getScopedVariable() {
        return scopedVariable;
    }

    public Set<Node<ScopedStatement>> getLiveNodes() {
        return liveNodes;
    }

    /** Gets the conflict graph for some live ranges. */
    public static Graph<LiveRange> conflictGraph(Collection<LiveRange> liveRanges) {
        ImmutableList.Builder<Node<LiveRange>> conflictGraphNodesBuilder =
                ImmutableList.builder();
        for (LiveRange liveRange : liveRanges) {
            conflictGraphNodesBuilder.add(Node.of(liveRange));
        }
        ImmutableList<Node<LiveRange>> conflictGraphNodes = conflictGraphNodesBuilder.build();

        Graph.Builder<LiveRange> conflictGraphBuilder = Graph.builder();
        // Loop over all pairs of live ranges. (We'll consider each pair twice.  It's easier.)
        for (Node<LiveRange> firstRange : conflictGraphNodes) {
            for (Node<LiveRange> secondRange : conflictGraphNodes) {
                if (conflict(firstRange.value(), secondRange.value())) {
                    conflictGraphBuilder.link(firstRange, secondRange);
                }
            }
        }
        return conflictGraphBuilder.build();
    }

    /** Returns whether two LiveRanges overlap. */
    private static boolean conflict(LiveRange first, LiveRange second) {
        return !Sets.intersection(first.getLiveNodes(), second.getLiveNodes()).isEmpty();
    }
}
