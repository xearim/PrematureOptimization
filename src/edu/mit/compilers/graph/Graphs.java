package edu.mit.compilers.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class Graphs {
    private Graphs() {}

    /** Return all nodes in 'graph' reachable from 'start', in DFS order. */
    public static <T> Iterable<Node<T>> dfs(Graph<T> graph, Node<T> start) {
        return dfs(graph, ImmutableList.of(start));
    }

    // TODO(jasonpr): Emit nodes lazily.
    /**
     * Return all nodes in 'graph' reachable from any node in 'starts', in DFS order.
     *
     * <p>The search from the first node in 'starts' is completed before the search
     * from the second node starts, and so on. 
     */
    public static <T> Set<Node<T>> dfs(Graph<T> graph, Collection<Node<T>> starts) {
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
}
