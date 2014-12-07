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


public class DefUseChain {

    private final Node<ScopedStatement> def;
    private final Node<ScopedStatement> use;

    public DefUseChain(Node<ScopedStatement> def, Node<ScopedStatement> use) {
        this.def = def;
        this.use = use;
    }

    public Node<ScopedStatement> getDef() {
        return def;
    }

    public Node<ScopedStatement> getUse() {
        return use;
    }

    @Override
    public String toString() {
        return "DefUseChain [def=" + def + ", use=" + use + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((def == null) ? 0 : def.hashCode());
        result = prime * result + ((use == null) ? 0 : use.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefUseChain)) {
            return false;
        }
        DefUseChain other = (DefUseChain) obj;
        if (def == null) {
            if (other.def != null) {
                return false;
            }
        } else if (!def.equals(other.def)) {
            return false;
        }
        if (use == null) {
            if (other.use != null) {
                return false;
            }
        } else if (!use.equals(other.use)) {
            return false;
        }
        return true;
    }

    /** Converts some reaching definitions to def-use chains, grouped by variable. */
    public static Multimap<ScopedVariable, DefUseChain>
            getDefUseChains(Multimap<Node<ScopedStatement>, ReachingDefinition> reachingDefs) {
        ImmutableMultimap.Builder<ScopedVariable, DefUseChain> builder =
                ImmutableMultimap.builder();
        for (Node<ScopedStatement> user : reachingDefs.keySet()) {
            for (ScopedVariable usedVar : localScalarDependencies(user.value())) {
                // Get all the defs for this node's use of this variable.
                // For each def, emit a def-use chain, using this 'user' node as the 'use'.
                for (Node<ScopedStatement> definer : usedReachingDefs(usedVar, reachingDefs.get(user))) {
                    builder.put(usedVar, new DefUseChain(definer, user));
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
