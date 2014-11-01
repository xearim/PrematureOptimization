package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

public class Subexpression {
    private NativeExpression ne;
    private Scope scope;

    /**
     * The Scope is the immediate scope.
     */
    public Subexpression(NativeExpression ne, Scope scope) {
        this.ne = ne;
        this.scope = scope;
    }

    /**
     * Finds the first scope that contains a variable from the subexpression
     * @return
     */
    public Scope getGeneralScope() {
        Scope s = this.scope;
        Set<Location> variables = getVariables(this.ne);

        do {
            if (containsAVariable(s, variables)) {
                return s;
            }
        } while (s.getParent().isPresent());
        
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

    private Set<Location> getVariables(NativeExpression ne) {
        throw new RuntimeException("Subexpression#getVariables unimplemented.");
    }
}
