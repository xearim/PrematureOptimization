package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.Scope;

/** A reference to a variable, in a certain scope. */
public class VariableReference implements Value{
    private final String name;
    /** The scope in which this variable is referenced. */
    private final Scope scope;

    public VariableReference(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public String inAttSyntax() {
        switch(scope.getScopeType()){
		case GLOBAL:
			return name;
		case LOCAL:
			return Long.toString(scope.offsetFromBasePointer(name)) + "(" + Register.RBP.inAttSyntax() + ")";
		case PARAMETER:
			// TODO(xearim): Make lookup to what register or offset the parameter is at, return that
			return "$0";
		default:
			throw new AssertionError("Invalid Scope for Variable Reference: " + scope.getScopeType());
        }
    }
}
