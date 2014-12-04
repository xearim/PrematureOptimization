package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public interface AnalysisSpec<T> {
    public Multimap<Node<ScopedStatement>, T> getGenSets(Set<Node<ScopedStatement>> statementNodes);
    public Multimap<Node<ScopedStatement>, T> getKillSets(Set<Node<ScopedStatement>> statementNodes);
    public Set<T> applyConfluenceOperator(Iterable<Collection<T>> inputs);
    public Set<T> applyTransferFunction(Collection<T> gen, Collection<T> input, Collection<T> kill);
    public Set<Node<ScopedStatement>> filterNodes(Iterable<Node<ScopedStatement>> nodes);
}
