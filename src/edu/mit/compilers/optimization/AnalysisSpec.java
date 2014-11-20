package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;

public interface AnalysisSpec<T> {
    public Multimap<DataFlowNode, T> getGenSets(Set<StatementDataFlowNode> statementNodes);
    public Multimap<DataFlowNode, T> getKillSets(Set<StatementDataFlowNode> statementNodes);
    public Set<T> getOriginalOutSet(Set<StatementDataFlowNode> nodes);
    public Set<T> getInSet(Iterable<Set<T>> outSets, Set<T> seed);
    public Set<T> getOutSetFromInSet(Collection<T> gen, Collection<T> in, Collection<T> kill);
}
