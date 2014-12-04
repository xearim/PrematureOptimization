package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.intersection;
import static edu.mit.compilers.optimization.Util.containsMethodCall;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class AvailabilitySpec implements AnalysisSpec<ScopedExpression> {
    /**
     * Map each node to its GEN set of Subexpressions, filtering out Expressions
     * that contain MethodCalls.
     *
     * <p>Requires that the statements all have an expression.
     */
    @Override
    public Multimap<Node<ScopedStatement>, ScopedExpression> getGenSets(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<Node<ScopedStatement>,ScopedExpression> builder =
                ImmutableMultimap.builder();

        for (Node<ScopedStatement> node : statementNodes) {
            // Design Decision: don't recurse into method calls
            NativeExpression ne = node.value().getStatement().getExpression();
            if (containsMethodCall(ne)) {
                /*
                 * "b+foo()" can return different values on different calls,
                 * so we don't want to claim that "b+foo()" is available.
                 */
                continue;
            }

            builder.put(node, new ScopedExpression(ne, node.value().getScope()));
        }

        return builder.build();
    }

    @Override
    public boolean mustKill(Node<ScopedStatement> curNode, ScopedExpression candidate) {
        Set<ScopedLocation> victimVariables = getPotentiallyChangedVariables(curNode);
        for (ScopedLocation victimVariable : victimVariables) {
            if (candidate.uses(victimVariable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the union of all the sets.
     */
    @Override
    public Set<ScopedExpression> applyConfluenceOperator(Iterable<Collection<ScopedExpression>> outSets) {
        return intersection(outSets);
    }

    /**
     * Returns (gen U in) - kill.
     */
    @Override
    public Set<ScopedExpression> applyTransferFunction(Collection<ScopedExpression> gen,
            Collection<ScopedExpression> input, Node<ScopedStatement> curNode) {
        Set<ScopedExpression> newOutSet = new HashSet<ScopedExpression>(input);
        newOutSet.addAll(gen);

        Set<ScopedExpression> toKill = Sets.newHashSet();
        for (ScopedExpression candidate : newOutSet) {
            if (mustKill(curNode, candidate)) {
                toKill.add(candidate);
            }
        }
        newOutSet.removeAll(toKill);
        return newOutSet;
    }

    /** Filters all the nodes that do not have an expression. */
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

    /**
     * Maps StatementNode<ScopedStatement>s to variables they may change during
     * execution.
     */
    private static Set<ScopedLocation> getPotentiallyChangedVariables(
            Node<ScopedStatement> node) {
        ImmutableSet.Builder<ScopedLocation> builder = ImmutableSet.builder();
        Set<ScopedLocation> globals = null; //getGlobals(node);

            StaticStatement statement = node.value().getStatement();
            if (statement instanceof Assignment) {
                builder.add(ScopedLocation.getAssigned(
                        (Assignment) node.value().getStatement(), node.value().getScope()));
            }

            if (Util.containsMethodCall(statement.getExpression())) {
                builder.addAll(globals);
            }

        return builder.build();
    }

    /**
     * Maps variables to expressions that contain them.
     */
    private static Multimap<ScopedLocation, ScopedExpression> getExpressionsContaining(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<ScopedExpression, ScopedLocation> variablesIn = ImmutableMultimap.builder();

        for (Node<ScopedStatement> node : statementNodes) {
            ScopedExpression newSubexpr = new ScopedExpression(node.value().getStatement().getExpression(), node.value().getScope());
            variablesIn.putAll(newSubexpr, newSubexpr.getVariables());
        }

        return variablesIn.build().inverse();
    }

    /**
     * Returns the set of all global variables. Intended for generating kill
     * sets of StatementNode<ScopedStatement>s containing MethodCalls.
     */
    private static Set<ScopedLocation> getGlobals(Set<Node<ScopedStatement>> nodes) {
        Set<ScopedLocation> globalVars = new HashSet<ScopedLocation>();
        for (Node<ScopedStatement> node : nodes){
            for (ScopedLocation var :
                ScopedLocation.getVariablesOf(node.value())){
                if (var.isGlobal()){
                    globalVars.add(var);
                }
            }
        }

        return ImmutableSet.copyOf(globalVars);
    }
}
