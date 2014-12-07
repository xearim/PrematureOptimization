package edu.mit.compilers.optimization;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class ReachingDefinition {
    private final ScopedVariable sc;
    private final Node<ScopedStatement> node;
    
    public ReachingDefinition(ScopedVariable sc, Node<ScopedStatement> node) {
        this.sc = sc;
        this.node = node;
    }

    public ScopedVariable getScopedVariable() {
        return sc;
    }

    public Node<ScopedStatement> getNode() {
        return node;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((sc == null) ? 0 : sc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReachingDefinition other = (ReachingDefinition) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (sc == null) {
            if (other.sc != null)
                return false;
        } else if (!sc.equals(other.sc))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReachingDefinition [sc=" + sc + ", node=" + node + "]";
    }
}
