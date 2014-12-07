package edu.mit.compilers.optimization.loops;

import static edu.mit.compilers.common.SetOperators.intersection;
import static edu.mit.compilers.optimization.DataFlowAnalyzer.DOMINATORS;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BasicDiGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.AnalysisSpec;

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
    /*
     * TODO(Manny): consider switching ScopedStatement to generic
     * Problem: When instantiating DataFlowAnalyzer, need to specify type.
     * Cannot keep type as a generic afterward.
     */
    public static DiGraph<ScopedStatement> getDominatorTree(
            BcrFlowGraph<ScopedStatement> graph) {

        Multimap<Node<ScopedStatement>,Node<ScopedStatement>> dominatorsMap =
                DOMINATORS.calculate(graph);

        BasicDiGraph.Builder<ScopedStatement> builder =
                BasicDiGraph.<ScopedStatement>builder();
        for (Entry<Node<ScopedStatement>, Node<ScopedStatement>>  entry : dominatorsMap.entries()) {
            Node<ScopedStatement> conquered = entry.getKey();
            Node<ScopedStatement> dominator = entry.getValue();

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
    private static boolean shouldShowDominationEdge(
            BcrFlowGraph<ScopedStatement> graph,
            Multimap<Node<ScopedStatement>,Node<ScopedStatement>> dominatorMap,
            Node<ScopedStatement> dominator,
            Node<ScopedStatement> conquered) {

        for (Node<ScopedStatement> child : graph.getSuccessors(dominator)) {
            if (dominatorMap.get(conquered).contains(child)) {
                return false;
            }
        }
        return true;
    }
}
