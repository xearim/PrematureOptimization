package edu.mit.compilers.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SourceCounter {
    private Map<ControlFlowNode, Integer> sourceCounts;
    
    /** Get the set of nodes that are the 'next' node of multiple other nodes. */
    public Set<ControlFlowNode> getMultiSourceNodes(ControlFlowNode node) {
        // Reset sourceCounts
        sourceCounts = new HashMap<ControlFlowNode, Integer>();

        countFrom(node);
        
        ImmutableSet.Builder<ControlFlowNode> resultBuilder = ImmutableSet.builder();
        for (ControlFlowNode sink : sourceCounts.keySet()) {
            if (sourceCounts.get(sink) > 1) {
                resultBuilder.add(sink);
            }
        }
        return resultBuilder.build();
    }
    
    private void countFrom(ControlFlowNode node) {
        if (sourceCounts.containsKey(node)) {
            sourceCounts.put(node, 1 + sourceCounts.get(node));
            // Do not recurse!  We've already recursed from this node before.
        } else {
            sourceCounts.put(node, 1);
            for (ControlFlowNode child: node.getSinks()) {
                countFrom(child);
            }
        }
    }
}
