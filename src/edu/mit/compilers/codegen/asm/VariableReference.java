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
        // TODO(manny): Figure out how to do this.  Or rearchitect.
        throw new RuntimeException("Not yet implemented.");
    }
}
