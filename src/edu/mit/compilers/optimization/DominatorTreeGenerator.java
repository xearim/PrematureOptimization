package edu.mit.compilers.optimization;

import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import edu.mit.compilers.graph.BasicDiGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.loops.DominatorSpec;

public class DominatorTreeGenerator {

    /**
     * Returns a DiGraph that represents a DominatorTree. Every edge
     * means that the source dominates the sink, but none of the
     * source node's children dominate the sink node.
     */
    public static <T> DiGraph<T> getDominatorTree(
            BcrFlowGraph<T> graph) {

        Multimap<Node<T>, Node<T>> dominatorsMap =
                new DataFlowAnalyzer<T,Node<T>>(new DominatorSpec<T>()).calculate(graph);

        BasicDiGraph.Builder<T> builder =
                BasicDiGraph.<T>builder();
        for (Entry<Node<T>, Node<T>>  entry : dominatorsMap.entries()) {
            Node<T> conquered = entry.getKey();
            Node<T> dominator = entry.getValue();

            if (shouldShowDominationEdge(graph,dominatorsMap,dominator,conquered)) {
                builder.link(dominator,conquered);
            }
        }

        return builder.build();
    }

    /**
     * Returns true if none of the children of the dominator also dominate
     * the conquered node.
     */
    private static <T> boolean shouldShowDominationEdge(
            BcrFlowGraph<T> graph,
            Multimap<Node<T>, Node<T>> dominatorsMap,
            Node<T> dominator,
            Node<T> conquered) {

        /*
         * Do not show the edge under either of these conditions:
         *
         * 1) If any of the children of the dominator also dominate the conquered.
         *
         * 2) none of the parents is the dominator
         * and only one of the parents of the conquered is
         * also dominated by the dominator, do not show the edge.
         */
        for (Node<T> child : graph.getSuccessors(dominator)) {
            if (dominatorsMap.get(conquered).contains(child)) {
                return false;
            }
        }

        int dominatedParents = 0;
        for (Node<T> parent: graph.getPredecessors(conquered)) {
            if (parent.equals(dominator)) {
                return true;
            }
            if (dominatorsMap.get(parent).contains(dominator)) {
                dominatedParents++;
            }
        }
        return dominatedParents != 1;
    }
}
