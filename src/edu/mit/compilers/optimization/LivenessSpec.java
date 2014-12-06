package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
import static edu.mit.compilers.optimization.Util.getRedefinedVariables;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.Scope;
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
    public Set<ScopedVariable> getGenSet(Node<ScopedStatement> node, Collection<ScopedVariable> inputs) {
        if (!node.hasValue()) {
            return ImmutableSet.of();
        }

        ScopedStatement scopedStatement = node.value();
        StaticStatement statement = scopedStatement.getStatement();

        // Do not gen anything if this is an assignment with a dead Left Hand Side.
        if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            ScopedVariable assignedVariable =
                    ScopedVariable.getAssigned(assignment, scopedStatement.getScope());
            if (!inputs.contains(assignedVariable)) {
                return ImmutableSet.of();
            }
        }

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

    /** Gets all the variables that this statement can read. */
    private Set<ScopedVariable> dependencies(ScopedStatement scopedStatement) {
        StaticStatement statement = scopedStatement.getStatement();
        Scope scope = scopedStatement.getScope();

        ImmutableSet.Builder<ScopedVariable> dependencies = ImmutableSet.builder();

        // The LHS is a dependency for statements like x += 1.
        if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            if (!assignment.getOperation().isAbsolute()) {
                dependencies.add(ScopedVariable.getAssigned(assignment, scope));
            }
        }

        // All the variables that are a part of the expression are dependenices.
        dependencies.addAll(ScopedVariable.getVariablesOf(scopedStatement));

        // A global method call depends on all the globals that that function reads.
        if (Util.containsMethodCall(statement.getExpression())) {
            // For now, just assume that functions can read every global!
            // TODO(jasonpr): Only add each function's global read set.
            dependencies.addAll(Util.getGlobalVariables(scope));
        }

        return dependencies.build();
    }
}
