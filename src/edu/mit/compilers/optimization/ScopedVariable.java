package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;

/**
 * Intended to keep the information about the location as well as the scope
 * that it is from. Will be used to determine the difference between two
 * variables with the same name but different scopes.
 */
public class ScopedVariable {
    Location location;
    Scope scope;

    private ScopedVariable (Location loc, Scope scope) {
        this.location = loc;
        this.scope = scope;
    }

    /** Returns the variable on the left of the assignment. */
    public static ScopedVariable getAssigned(AssignmentDataFlowNode assignmentNode) {
        Location loc = assignmentNode.getAssignment().getLocation();
        return new ScopedVariable(loc, getScopeOf(loc, assignmentNode.getScope()));
    }

    /** Returns the scope that location is in. */
    public static Scope getScopeOf(Location loc, Scope immediateScope) {
        throw new UnsupportedOperationException("Variable#getScopeOf unimplemented.");
    }

    /** Returns all the variables in the NativeExpression */
    public static Set<ScopedVariable> getVariablesOf(NativeExpression ne, Scope scope) {
        throw new UnsupportedOperationException("Variable#getVariablesOf unimplemented");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
        ScopedVariable other = (ScopedVariable) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }

}
