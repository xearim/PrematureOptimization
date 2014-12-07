package edu.mit.compilers.optimization.loops;

import static edu.mit.compilers.common.SetOperators.intersection;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.AnalysisSpec;

/**
 * The DominatorSpec class allows us to compute dominators. A dominator has
 * either of the following properties:
 * 1) A Node n dominates Node m if all paths to Mode m must
 * pass through Node n.
 * 2) Node n = Node m
 *
 * <p>By convention, we do not automatically put a node in its own input set.
 * Instead, we put a node in its generating set. This means that when checking
 * for dominators, we need to check either if the nodes are equivalent or one
 * is in the input set of the other.
 *
 * @param <N> - Typically a ScopedStatement.
 */
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
}
