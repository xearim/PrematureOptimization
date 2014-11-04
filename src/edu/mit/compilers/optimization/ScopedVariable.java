package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;
import edu.mit.compilers.common.Variable;

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
    
    public Location getLocation(){
    	return location;
    }
    
    public Scope getScope(){
    	return scope;
    }
    
    public boolean isGlobal(){
    	return scope.getScopeType() == ScopeType.GLOBAL;
    }

    /** Returns the variable on the left of the assignment. */
    public static ScopedVariable getAssigned(AssignmentDataFlowNode assignmentNode) {
    	Location loc = assignmentNode.getAssignment().getLocation();
        Variable var = loc.getVariable();
        return new ScopedVariable(loc, getScopeOf(var, assignmentNode.getScope()));
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
    	HashSet<ScopedVariable> variables = new HashSet<ScopedVariable>();
    	for(GeneralExpression geChild : ge.getChildren()){
    		if(geChild instanceof ArrayLocation){
    			ArrayLocation loc = ((ArrayLocation) geChild);
    			variables.add(new ScopedVariable(loc, getScopeOf(loc.getVariable(), scope)));
    			variables.addAll(getVariablesOf(loc.getIndex(), scope));
    		} else if (geChild instanceof ScalarLocation){
    			ScalarLocation loc = ((ScalarLocation) geChild);
    			variables.add(new ScopedVariable(loc, getScopeOf(loc.getVariable(), scope)));
    		} else {
    			variables.addAll(getVariablesOf(geChild, scope));
    		}
    	}
    	return variables;
    }
    
    public static Set<ScopedVariable> getVariablesOf(StatementDataFlowNode node) {
    	HashSet<ScopedVariable> variables = new HashSet<ScopedVariable>();
    	for(GeneralExpression expr : node.getExpressions()){
    		for(ScopedVariable var : ScopedVariable.getVariablesOf(expr, 
    						((StatementDataFlowNode) node).getScope())){
    			variables.add(var);
    		}
    	}
    	return variables;
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
