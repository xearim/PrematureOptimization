package edu.mit.compilers.common;

import java.util.Collection;
import java.util.HashSet;

import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.optimization.cse.VariableOrdering;

/**
 * A variable.
 * 
 * <p>This variable is not necessarily a user-defined variable.  It could also be
 * generated during the compilation phase.
 */
public class Variable {
    /** Where a variable came from. */
    private static enum VariableCreator { USER, COMPILER }
    private static VariableOrdering ORDER = VariableOrdering.BasicOrdering();
    
    private final VariableCreator creator;
    private final String name;
    
    private Variable(VariableCreator creator, String name) {
        this.creator = creator;
        this.name = name;
    }
    
    /**
     * Make a variable on behalf of the user.
     *
     * <p>For example, when the user writes 'int i', we invoke forUser("i").
     */
    public static Variable forUser(String name) {
    	Variable userVar = new Variable(VariableCreator.USER, name);
    	ORDER.addToOrdering(userVar);
        return userVar;
    }

    /**
     * Make a variable on behalf of the compiler.
     *
     * <p>We invoke this method when the compiler generates a variable, e.g.
     * when doing Common Subexpression Elimination.
     */
    public static Variable forCompiler(String name) {
    	Variable compilerVar = new Variable(VariableCreator.COMPILER, name);
    	ORDER.addToOrdering(compilerVar);
        return compilerVar;
    }
    
    public String asText() {
    	return creator == VariableCreator.USER 
    		   ? name
    		   : "C$" + name;
    }
    
    public int compareTo(Variable other){
    	return ORDER.compare(this, other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + creator.hashCode();
        result = prime * result + name.hashCode();
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
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) obj;
        if (creator != other.creator) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Variable [creator=" + creator + ", name=" + name + "]";
    }

    public String generateName() {
        if (creator == VariableCreator.USER) {
            return name;
        } else {
            return creator + "$" + name;
        }
    }
    
}
