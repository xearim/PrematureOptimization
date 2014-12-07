package edu.mit.compilers.regalloc;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;
import edu.mit.compilers.optimization.ReachingDefinition;
import edu.mit.compilers.optimization.ScopedVariable;
import edu.mit.compilers.optimization.Util;


public class DefUseChain<T> {

    private final Node<T> def;
    private final Node<T> use;

    public DefUseChain(Node<T> def, Node<T> use) {
        this.def = def;
        this.use = use;
    }

    public Node<T> getDef() {
        return def;
    }

    public Node<T> getUse() {
        return use;
    }

    /** Converts some reaching definitions to def-use chains, grouped by variable. */
    public static Multimap<ScopedVariable, DefUseChain<ScopedStatement>>
            getDefUseChains(Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs) {
        ImmutableMultimap.Builder<ScopedVariable, DefUseChain<ScopedStatement>> builder =
                ImmutableMultimap.builder();
        for (Node<ScopedStatement> user : reachingDefs.keySet()) {
            for (ScopedVariable usedVar : localScalarDependencies(user.value())) {
                // Get all the defs for this node's use of this variable.
                // For each def, emit a def-use chain, using this 'user' node as the 'use'.
                for (Node<ScopedStatement> definer : usedReachingDefs(usedVar, reachingDefs.get(user))) {
                    builder.put(usedVar, new DefUseChain<ScopedStatement>(definer, user));
                }
            }
        }
        return builder.build();
    }

    private static Set<Node<ScopedStatement>> usedReachingDefs(
            ScopedVariable variable, Collection<ReachingDefinition> reachingDefs) {
        ImmutableSet.Builder<Node<ScopedStatement>> builder = ImmutableSet.builder();
        for (ReachingDefinition reachingDef : reachingDefs) {
            if (reachingDef.getScopedVariable().equals(variable)) {
                builder.add(reachingDef.getNode());
            }
        }
        return builder.build();
    }

    /**
     * Get dependencies of a statement that are both local and scalar.
     *
     * <p>These are the variables that the register allocator can work with.
     */
    public static Set<ScopedVariable> localScalarDependencies(ScopedStatement scopedStatement) {
        ImmutableSet.Builder<ScopedVariable> localDeps = ImmutableSet.builder();
        Scope globalScope = scopedStatement.getScope().getGlobalScope();
        for (ScopedVariable scopedVar : Util.dependencies(scopedStatement)) {
            if (!scopedVar.getScope().equals(globalScope) && !scopedVar.isArray()) {
                localDeps.add(scopedVar);
            }
        }
        return localDeps.build();
    }
}
