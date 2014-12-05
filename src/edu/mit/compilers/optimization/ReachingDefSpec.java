package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

// Map<Node,Multimap<ScopedVariable,Node>>
public class ReachingDefSpec implements AnalysisSpec<ReachingDefinition> {

    /** Get the set of variables that are potentially redefined at a node. */
    private Set<ScopedLocation> redefined(Node<ScopedStatement> node) {
        ImmutableSet.Builder<ScopedLocation> redefinedBuilder = ImmutableSet.builder();

        ScopedStatement scopedStatement = node.value();
        StaticStatement statement = scopedStatement.getStatement();
        Scope scope = scopedStatement.getScope();
        // Add LHS.
        if (statement instanceof Assignment) {
            ScopedLocation lhs = ScopedLocation.getAssigned((Assignment) statement, scope);
            redefinedBuilder.add(lhs);
        }

        if (Util.containsMethodCall(statement.getExpression())) {
            // For now, just assume that functions can redefine every global!
            // TODO(jasonpr): Only add each function's global write set.
            redefinedBuilder.addAll(Util.getGlobalLocations(scope));
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
        for (ScopedLocation redefined : redefined(node)) {
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
        ImmutableSet.Builder<Node<ScopedStatement>> builder = ImmutableSet.builder();

        for (Node<ScopedStatement> node : nodes) {
            if (!node.hasValue()) {
                continue;
            }
            if (!node.value().getStatement().hasExpression()) {
                continue;
            }
            builder.add(node);
        }

        return builder.build();
    }

    @Override
    public boolean gensImmuneToKills() {
        return true;
    }
}
