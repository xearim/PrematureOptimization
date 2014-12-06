package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

// Map<Node,Multimap<ScopedVariable,Node>>
public class ReachingDefSpec implements AnalysisSpec<ScopedStatement, ReachingDefinition> {

    /** Get the set of variables that are potentially redefined at a node. */
    private Set<ScopedVariable> redefined(Node<ScopedStatement> node) {
        ImmutableSet.Builder<ScopedVariable> redefinedBuilder = ImmutableSet.builder();

        ScopedStatement scopedStatement = node.value();
        StaticStatement statement = scopedStatement.getStatement();
        Scope scope = scopedStatement.getScope();
        // Add LHS.
        if (statement instanceof Assignment) {
            ScopedVariable lhs = ScopedVariable.getAssigned((Assignment) statement, scope);
            redefinedBuilder.add(lhs);
        }

        if (Util.containsMethodCall(statement.getExpression())) {
            // For now, just assume that functions can redefine every global!
            // TODO(jasonpr): Only add each function's global write set.
            redefinedBuilder.addAll(Util.getGlobalVariables(scope));
        }
        return redefinedBuilder.build();
    }

    /**
     * Returns whether the reaching definition's variable is redefined at this node.
     * i.e. returns redefined.curNode.contains(reachingDef.getVariable())
     * */
    @Override
    public boolean mustKill(Node<ScopedStatement> curNode, ReachingDefinition reachingDef) {
        return redefined(curNode).contains(reachingDef.getScopedLocation());
    }

    @Override
    public Set<ReachingDefinition> getGenSet(Node<ScopedStatement> node) {
        ImmutableSet.Builder<ReachingDefinition> builder = ImmutableSet.builder();
        for (ScopedVariable redefined : redefined(node)) {
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
