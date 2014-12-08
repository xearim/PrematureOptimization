package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.union;
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
    public Set<ScopedVariable> getGenSet(Node<ScopedStatement> node, Collection<ScopedVariable> inputs) {
        if (!node.hasValue()) {
            return ImmutableSet.of();
        }

        ScopedStatement scopedStatement = node.value();
        StaticStatement statement = scopedStatement.getStatement();

        // Do not gen anything if this is an assignment with a dead Left Hand Side...
        // unless the Right Hand Side has a (possibly side-effect-ful) method call.
        if (statement instanceof Assignment
                && !Util.containsMethodCall(statement.getExpression())) {
            Assignment assignment = (Assignment) statement;
            ScopedVariable assignedVariable =
                    ScopedVariable.getAssigned(assignment, scopedStatement.getScope());
            if (!inputs.contains(assignedVariable) && 
            		!Util.getGlobalVariables(scopedStatement.getScope()).contains(assignedVariable)) {
                return ImmutableSet.of();
            }
        }
        
        //System.out.println(node.contentString());
        //System.out.println(Util.dependencies(node.value()));

        return Util.dependencies(node.value());
    }

    /** Kills a ScopedVariable if it was defined. */
    @Override
    public boolean mustKill(Node<ScopedStatement> currentNode,
            ScopedVariable candidate) {
        // Do not kill arrays.  (We don't want A[5] to kill A[4].)
        return getRedefinedVariables(currentNode).contains(candidate) && !candidate.isArray();
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
}
