package edu.mit.compilers.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class Graphs {
    private Graphs() {}

    // TODO(jasonpr): Decide whether it's bad for us to expose a synonym of dfs. Consider
    // Deque#pop and Deque#addFirst.
    /** Returns all nodes in 'graph' reachable from 'start'. */
    public static <T> Set<Node<T>> reachable(DiGraph<T> graph, Node<T> start) {
        return dfs(graph, start);
    }

    /** Return all nodes in 'graph' reachable from 'start', in DFS order. */
    public static <T> Set<Node<T>> dfs(DiGraph<T> graph, Node<T> start) {
        return dfs(graph, ImmutableList.of(start));
    }

    // TODO(jasonpr): Emit nodes lazily.
    /**
     * Return all nodes in 'graph' reachable from any node in 'starts', in DFS order.
     *
     * <p>The search from the first node in 'starts' is completed before the search
     * from the second node starts, and so on. 
     */
    public static <T> Set<Node<T>> dfs(DiGraph<T> graph, Collection<Node<T>> starts) {
        Set<Node<T>> visited = Sets.newHashSet();
        Deque<Node<T>> agenda = new ArrayDeque<Node<T>>(starts);

        while (!agenda.isEmpty()) {
            Node<T> node = agenda.pop();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);

            for (Node<T> child : graph.getSuccessors(node)) {
                agenda.push(child);
            }
        }

        return visited;
    }

    /**
     * Returns a new graph such that every edge (a, b) of the original graph
     * becomese (b, a) in the new graph. */
    public static <T> DiGraph<T> inverse(final DiGraph<T> graph) {
        return new DiGraph<T>() {
            @Override
            public Set<Node<T>> getNodes() {
                return graph.getNodes();
            }

            @Override
            public Iterable<Node<T>> getPredecessors(Node<T> node) {
                return graph.getSuccessors(node);
            }

            @Override
            public Iterable<Node<T>> getSuccessors(Node<T> node) {
                return graph.getPredecessors(node);
            }
        };
    }

    /** Gets all nodes reachable from the seed node. */
    public static <T> Set<Node<T>> connectedComponent(Graph<T> graph, Node<T> seed) {
        return dfs(graph, seed);
    }

    /** Gets the connected components of the graph. */
    public static <T> Collection<Set<Node<T>>> connectedComponents(Graph<T> graph) {
        ImmutableList.Builder<Set<Node<T>>> builder = ImmutableList.builder();
        Set<Node<T>> seen = Sets.newHashSet();
        for (Node<T> seed : graph.getNodes()) {
            if (seen.contains(seed)) {
                continue;
            }
            Set<Node<T>> connectedComponent = connectedComponent(graph, seed);
            seen.addAll(connectedComponent);
            builder.add(connectedComponent);
        }
        return builder.build();
    }

    /**
     * Computes the convex hull around 'start' and 'end'.
     *
     * <p>That is, a node 'N' is in the return set if there is any path, 'start'...'N'...'end'.
     */
    public static <T> Set<Node<T>> convexHull(Graph<T> graph, Node<T> start, Node<T> end) {
        return Sets.intersection(reachable(graph, start), reachable(inverse(graph), end));
    }
}
