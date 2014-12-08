package edu.mit.compilers.graph;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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
     * <p>That is, a node 'N' is in the return set if there is any path, 'start'...'N'...'end',
     * including 'start' and 'end'.
     */
    public static <T> Set<Node<T>> closedRange(DiGraph<T> graph, Node<T> start, Node<T> end) {
        return Sets.intersection(reachable(graph, start), reachable(inverse(graph), end));
    }

    public static class UncolorableGraphException extends Exception {
        private static final long serialVersionUID = 1L;
        public UncolorableGraphException(String message) {
            super(message);
        }
    }

    /** Return a coloring of 'graph', using the specified colors. */
    public static <T, C> Map<Node<T>, C> colored(Graph<T> graph, Set<C> colors)
            throws UncolorableGraphException {
        int numColors = colors.size();

        MutableGraph<T> workingGraph = new MutableGraph<T>(graph);
        Deque<Node<T>> removedNodes = new ArrayDeque<Node<T>>();

        // Remove all the nodes.
        boolean stillRemoving = true;
        while (stillRemoving) {
            // If we don't remove anything by the end of the iteration, then either we've
            // removed everything, or we cannot remove any more.
            stillRemoving = false;
            // Copy the nodes of the current working graph's state
            // to avoid concurrent modifications.
            for (Node<T> candidate : ImmutableSet.copyOf(workingGraph.getNodes())) {
                if (workingGraph.degree(candidate) < numColors) {
                    workingGraph.remove(candidate);
                    removedNodes.push(candidate);
                    stillRemoving = true;
                }
            }
        }
        if (workingGraph.getNodes().size() > 0) {
            throw new UncolorableGraphException("Could not color graph with " + numColors + " colors.");
        }

        // Color them all!
        Map<Node<T>, C> nodeColors = Maps.newHashMap();
        for (Node<T> node : removedNodes) {
            ImmutableSet.Builder<C> neighborColorsBuilder = ImmutableSet.builder();
            for (Node<T> neighbor : graph.getSuccessors(node)) {
                if (nodeColors.containsKey(neighbor)) {
                    // The neighbor has already been colored.
                    neighborColorsBuilder.add(nodeColors.get(neighbor));
                }
            }
            Set<C> neighborColors = neighborColorsBuilder.build();
            C unusedColor = unusedElement(colors, neighborColors);
            nodeColors.put(node, unusedColor);
        }

        return ImmutableMap.copyOf(nodeColors);
    }

    /**
     * Returns a graph where every node is adjacent to every other node.
     *
     * <p>Does not produce self loops.
     */
    public static <T> Graph<T> totalGraph(Iterable<Node<T>> nodes) {
        Graph.Builder<T> graphBuilder = Graph.builder();
        for (Node<T> node1 : nodes) {
            for (Node<T> node2 : nodes) {
                if (!node1.equals(node2)) {
                    graphBuilder.link(node1, node2);
                }
            }
        }
        return graphBuilder.build();
    }

    private static <E> E unusedElement(Set<E> allItems, Set<E> usedItems) {
        Set<E> unusedItems = Sets.difference(allItems, usedItems);
        checkState(!unusedItems.isEmpty());
        return Iterables.get(unusedItems, 0);
    }
}
