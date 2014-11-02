package edu.mit.compilers.codegen.dataflow;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.codegen.DataFlowNode;

public class DataFlowUtil {

    private DataFlowUtil() {}

    /**
     * Get all nodes reachable from some start node.
     *
     * <p>Sometimes we pass around a DataFlowNode that represents the entire
     * graph of DataFlowNodes reachable from it.  This method allows us to
     * enumerate all those nodes.
     */
    public static Collection<DataFlowNode> reachableFrom(DataFlowNode start) {
        // Just do DFS and return the visited set.
        Set<DataFlowNode> visited = new HashSet<DataFlowNode>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(start);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (visited.contains(node)) {
                continue;
            }
            for (DataFlowNode child : node.getSuccessors()) {
                queue.push(child);
            }
        }
        return ImmutableSet.copyOf(visited);
    }
}