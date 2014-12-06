package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.filterNodesWithoutExpressions;
import static edu.mit.compilers.optimization.Util.getRedefinedVariables;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class LivenessSpec implements AnalysisSpec<ScopedStatement, ScopedVariable> {

    /** Liveness algorithm propagates backwards */
    public boolean isForward() {
        return false;
    }

    /** A Variable is generated if it is used by this statement */
    @Override
    public Set<ScopedVariable> getGenSet(Node<ScopedStatement> node) {
        if (!node.hasValue()) {
            return ImmutableSet.<ScopedVariable>of();
        }
        // DO NOT SUBMIT: Do not gen the LHS if the RHS is not live!
        return dependencies(node.value());
    }

    /** Kills a ScopedVariable if it was defined. */
    @Override
    public boolean mustKill(Node<ScopedStatement> currentNode,
            ScopedVariable candidate) {
        return getRedefinedVariables(currentNode).contains(candidate);
    }

    @Override
    public Set<ScopedVariable> applyConfluenceOperator(
            Iterable<Collection<ScopedVariable>> inputs) {
        return union(inputs);
    }

    @Override
    public boolean gensImmuneToKills() {
        return true;
    }

    @Override
    public Set<Node<ScopedStatement>> filterNodes(
            Iterable<Node<ScopedStatement>> nodes) {
        return filterNodesWithoutExpressions(nodes);
    }

    private Set<ScopedVariable> dependencies(ScopedStatement scopedStatement) {
        StaticStatement statement = scopedStatement.getStatement();
        ImmutableSet.Builder<ScopedVariable> dependencies = ImmutableSet.builder();
        if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            if (!assignment.getOperation().isAbsolute()) {
                dependencies.add(ScopedVariable.getAssigned(assignment, scopedStatement.getScope()));
            }
        }
        dependencies.addAll(ScopedVariable.getVariablesOf(scopedStatement));
        return dependencies.build();
    }
}
