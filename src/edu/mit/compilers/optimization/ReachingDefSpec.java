package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;
import static edu.mit.compilers.optimization.Util.getRedefinedVariables;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

// Map<Node,Multimap<ScopedVariable,Node>>
public class ReachingDefSpec implements AnalysisSpec<ScopedStatement, ReachingDefinition> {

    /**
     * Returns whether the reaching definition's variable is redefined at this node.
     * i.e. returns redefined.curNode.contains(reachingDef.getVariable())
     * */
    @Override
    public boolean mustKill(Node<ScopedStatement> curNode, ReachingDefinition reachingDef) {
        return getRedefinedVariables(curNode).contains(reachingDef.getScopedLocation());
    }

    @Override
    public Set<ReachingDefinition> getGenSet(Node<ScopedStatement> node) {
        ImmutableSet.Builder<ReachingDefinition> builder = ImmutableSet.builder();
        for (ScopedVariable redefined : getRedefinedVariables(node)) {
            // Each redefined variable is gets a definition... at this node!
            builder.add(new ReachingDefinition(redefined, node));
        }
        return builder.build();
    }

    @Override
    public Set<ReachingDefinition> applyConfluenceOperator(
            Iterable<Collection<ReachingDefinition>> inputs) {
        return union(inputs);
    }

    @Override
    public Set<Node<ScopedStatement>> filterNodes(
            Iterable<Node<ScopedStatement>> nodes) {
        return filterNodesWithoutExpressions(nodes);
    }

    @Override
    public boolean gensImmuneToKills() {
        return true;
    }

    /** Reaching Definition algorithm propagates forward */
    public boolean isForward() {
        return true;
    }
}
