package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

/**
 * Contains the native expression and its immediate scope. It is necessary to
 * keep track of the scope for a native expression in cases such as the
 * following:
 * - two if statements at the same level use the same variable name.
 * - an expressions is derived only from variables from higher scopes.
 */
public class ScopedExpression {
    private final NativeExpression ne;
    private final Scope scope;
    private final Set<ScopedLocation> variables;

    /**
     * The Scope is the immediate scope.
     */
    public ScopedExpression(NativeExpression ne, Scope scope) {
        this.ne = ne;
        this.variables = ScopedLocation.getVariablesOf(ne, scope);
        this.scope = getGeneralScope(scope);
    }

    /**
     * Finds the first scope that contains a variable from the subexpression
     */
    public Scope getGeneralScope(Scope scope) {
        Set<ScopedLocation> variables = getVariables();

        if (variables.isEmpty()) {
            return scope.getGlobalScope();
        }

        // Check immediate scope
        if (containsAVariable(scope, variables)) {
            return scope;
        }

        /*
         * If no variable is in immediate scope, recurse through scopes until
         * a scope that contains at least one variable is found.
         */
        Scope s = scope;
        while (s.getParent().isPresent()) {
            s = s.getParent().get();
            if (containsAVariable(s, variables)) {
                return s;
            }
        }

        throw new AssertionError("Subexpression.java: Subexpression variables don't exist in Scope.");
    }

    /**
     * Returns true if the Scope contains any of the variables from the
     * global NativeExpression ne.
     */
    private boolean containsAVariable(Scope s, Set<ScopedLocation> variables) {
        for(ScopedLocation var : variables){
        	if(s.isInScopeImmediately(var.getLocation().getVariable())){
        		return true;
        	}
        }
        return false;
    }
    public boolean uses(ScopedLocation scopedLocation) {
        return this.variables.contains(scopedLocation);
    }

    public Set<ScopedLocation> getVariables() {
        return this.variables;
    }

    public NativeExpression getNativeExpression() {
        return this.ne;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ne == null) ? 0 : ne.hashCode());
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
        ScopedExpression other = (ScopedExpression) obj;
        if (ne == null) {
            if (other.ne != null)
                return false;
        } else if (!ne.equals(other.ne))
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }
}
