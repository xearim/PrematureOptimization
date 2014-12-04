package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.difference;
import static edu.mit.compilers.common.SetOperators.union;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Location;
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
            Scope globalScope = scope.getGlobalScope();
            for (FieldDescriptor descriptor : globalScope.getVariables()) {
                // TODO(jasonpr): Find a way to avoid treating every array slot separately.
                // Every location of the global could potentially be written.
                for (Location location : descriptor.getLocations()) {
                    redefinedBuilder.add(new ScopedLocation(location, globalScope));
                }
            }
        }
        return redefinedBuilder.build();
    }

    @Override
    public Multimap<Node<ScopedStatement>, ReachingDefinition> getGenSets(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<Node<ScopedStatement>, ReachingDefinition> defsBuilder =
                ImmutableMultimap.builder();
        for (Node<ScopedStatement> node : statementNodes) {
            for (ScopedLocation redefined : redefined(node)) {
                // Each redefined variable is gets a definition... at this node!
                defsBuilder.put(node, new ReachingDefinition(redefined, node));
            }
        }
        return defsBuilder.build();
    }

    @Override
    public Multimap<Node<ScopedStatement>, ReachingDefinition> getKillSets(
            Set<Node<ScopedStatement>> statementNodes) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public Set<ReachingDefinition> applyConfluenceOperator(
            Iterable<Collection<ReachingDefinition>> inputs) {
        return union(inputs);
    }

    // Suppress generic varargs warning.
    @SuppressWarnings("unchecked")
    @Override
    public Set<ReachingDefinition> applyTransferFunction(
            Collection<ReachingDefinition> gen,
            Collection<ReachingDefinition> input,
            Collection<ReachingDefinition> kill) {
        return union(gen, difference(input, kill));
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
}
