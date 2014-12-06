package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;

/**
 * Intended to keep the information about the location as well as the scope
 * that it is from. Will be used to determine the difference between two
 * variables with the same name but different scopes.
 */
public class ScopedVariable {
    Variable variable;
    Scope scope;

    public ScopedVariable(Variable var, Scope scope) {
        this.variable = var;
        this.scope = scope;
    }
    
    public Variable getVariable(){
    	return variable;
    }
    
    public Scope getScope(){
    	return scope;
    }
    
    public boolean isGlobal(){
    	return scope.getScopeType() == ScopeType.GLOBAL;
    }

    /** Returns the variable on the left of the assignment. */
    public static ScopedVariable getAssigned(Assignment assignment, Scope scope) {
    	Variable var = assignment.getLocation().getVariable();
        return new ScopedVariable(var, getScopeOf(var, scope));
    }

    /** Returns the scope that variable is in. */
    public static Scope getScopeOf(Variable var, Scope immediateScope) {
    	if(immediateScope.isInScopeImmediately(var)){
    		return immediateScope;
    	}
    	for(Optional<Scope> parentScope = immediateScope.getParent(); 
    		parentScope.isPresent(); parentScope = parentScope.get().getParent()){
    		if(parentScope.get().isInScopeImmediately(var)){
    			return parentScope.get();
    		}
    	}
    	throw new AssertionError("Variable is not in any scope associated with immediateScope");
    }

    /** Returns all the variables in the GeneralExpression */
    public static Set<ScopedVariable> getVariablesOf(GeneralExpression ge, Scope scope) {
        if (ge instanceof Location) {
            Variable var = ((Location) ge).getVariable();
            return ImmutableSet.of(new ScopedVariable(var, getScopeOf(var, scope)));
        }
        ImmutableSet.Builder<ScopedVariable> builder = ImmutableSet.builder();
        for(GeneralExpression geChild : ge.getChildren()){
            builder.addAll(getVariablesOf(geChild, scope));
        }
        return builder.build();
    }

    public static Set<ScopedVariable> getVariablesOf(ScopedStatement statement) {
        return getVariablesOf(statement.getStatement().getExpression(), statement.getScope());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((variable == null) ? 0 : variable.hashCode());
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
        if (variable == null) {
            if (other.variable != null)
                return false;
        } else if (!variable.equals(other.variable))
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }

}
