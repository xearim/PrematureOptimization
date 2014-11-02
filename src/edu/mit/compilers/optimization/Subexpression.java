package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

/**
 * Contains the native expression and its immediate scope. It is necessary to
 * keep track of the scope for a native expression in cases such as the
 * following:
 * - two if statements at the same level use the same variable name.
 * - an expressions is derived only from variables from higher scopes.
 */
public class Subexpression {
    private final NativeExpression ne;
    private final Scope scope;

    /**
     * The Scope is the immediate scope.
     */
    public Subexpression(NativeExpression ne, Scope scope) {
        this.ne = ne;
        this.scope = getGeneralScope(scope);
    }

    /**
     * Finds the first scope that contains a variable from the subexpression
     */
    public Scope getGeneralScope(Scope scope) {
        Set<Location> variables = getVariables();

        // Check immediate scope
        if (containsAVariable(scope, variables)) {
            return scope;
        }

        /*
         * If no variable is in immediate scope, recurse through scopes until
         * a scope that contains at least on variable is found.
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

    public boolean containsMethodCall() {

        throw new UnsupportedOperationException("Subexpression#containsMethodCall unimplemented.");
    }

    /**
     * Returns true if the Scope contains any of the variables from the
     * global NativeExpression ne.
     */
    private boolean containsAVariable(Scope s, Set<Location> variables) {
        throw new RuntimeException("Subexpression#containsAVariableFromNativeExpression unimplemented.");
    }

    private Set<Location> getVariables() {
        throw new RuntimeException("Subexpression#getVariables unimplemented.");
    }

    public NativeExpression getNativeExpression() {
        return this.ne;
    }

    public Scope getScope() {
        return scope;
    }

    //TODO(Manny): implement equals and hashcode
//    public boolean equals(Subexpression se) {
//        //        return this.ne.equals(se.getNativeExpresion());
//        //                && this.scope() == se.getScope()
//        throw new UnsupportedOperationException("Subexpression#equals unimplemented.");
//    }
}
