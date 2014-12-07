package edu.mit.compilers.optimization.loops;

import static edu.mit.compilers.common.SetOperators.intersection;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.graph.BasicDiGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.AnalysisSpec;
import edu.mit.compilers.optimization.DataFlowAnalyzer;

public class DominatorSpec <N> implements AnalysisSpec<N, Node<N>> {

    @Override
    public boolean isForward() {
        return true;
    }

    @Override
    public Set<Node<N>> getGenSet(Node<N> node, Collection<Node<N>> inputs) {
        // Each node generates itself.
        return ImmutableSet.of(node);
    }

    @Override
    public boolean mustKill(Node<N> currentNode, Node<N> candidate) {
        // Never kill a node.
        return false;
    }

    @Override
    public Set<Node<N>> applyConfluenceOperator(Iterable<Collection<Node<N>>> inputs) {
        /*
         * Node n dominates Node m means that
         * ALL paths to Node m must pass through
         * Node n.
         */
        return intersection(inputs);
    }

    @Override
    public boolean gensImmuneToKills() {
        // Irrelevant, kill sets are empty.
        return false;
    }

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

        for (Node<T> child : graph.getSuccessors(dominator)) {
            if (dominatorsMap.get(conquered).contains(child)) {
                return false;
            }
        }
        return true;
    }
}
