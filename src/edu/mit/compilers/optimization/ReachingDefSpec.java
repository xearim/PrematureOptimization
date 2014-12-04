package edu.mit.compilers.optimization;

import static edu.mit.compilers.common.SetOperators.difference;
import static edu.mit.compilers.common.SetOperators.union;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

// Map<Node,Multimap<ScopedVariable,Node>>
public class ReachingDefSpec implements AnalysisSpec<ReachingDefinition> {

    @Override
    public Multimap<Node<ScopedStatement>, ReachingDefinition> getGenSets(
            Set<Node<ScopedStatement>> statementNodes) {
        throw new UnsupportedOperationException("unimplemented");
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
