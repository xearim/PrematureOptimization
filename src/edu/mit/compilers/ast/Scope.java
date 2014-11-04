package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.common.Variable;

public class Scope {
	
	private Optional<Scope> parent;
	private final ImmutableList<FieldDescriptor> entries;
	private final boolean ofLoop;
	
	public Scope(List<FieldDescriptor> variables) {
        this.entries = ImmutableList.copyOf(variables);
        this.parent = Optional.<Scope>absent();
        this.ofLoop = false;
    }
	
	public Scope(List<FieldDescriptor> variables, Scope parent) {
        this.entries = ImmutableList.copyOf(variables);
        this.parent = Optional.of(parent);
        this.ofLoop = false;
        
        for(FieldDescriptor variable : variables){
        	if(variable.getLength().isPresent()){
        		Architecture.CONTAINS_ARRAYS = true;
        		return;
        	}
        }
    }
	
	public Scope(List<FieldDescriptor> variables, Scope parent, boolean ofLoop) {
        this.entries = ImmutableList.copyOf(variables);
        this.parent = Optional.of(parent);
        this.ofLoop = ofLoop;
        
        for(FieldDescriptor variable : variables){
        	if(variable.getLength().isPresent()){
        		Architecture.CONTAINS_ARRAYS = true;
        		return;
        	}
        }
    }
	
    public Optional<Scope> getParent() {
        return parent;
    }

	public void setParent(Scope parent){
		this.parent = Optional.of(parent);
	}
	
	public ImmutableList<FieldDescriptor> getVariables() {
		return entries;
	}
	/**
	 * Checks to see if a given identifier corresponds to an actually initialized variable
	 * visible from this scope looking upwards
	 * 
	 * @param identifier - the target variable
	 */
	public boolean isInScope(Variable identifier){
        for (FieldDescriptor entry : entries) {
			if(identifier.equals(entry.variable))
				return true;
		}
		return (parent.isPresent() && parent.get().isInScope(identifier));
	}
	
	   /**
     * Return whether this scope has an entry for the given variable name. 
     *
     * <p>This method ignores any parent scopes. 
     */
    public boolean isInScopeImmediately(Variable variable) {
        for (FieldDescriptor entry : entries) {
            if (variable.equals(entry.variable)) {
                return true;
            }
        }
        return false;
    }
    
    public ScopeType getScopeType() {
        return parent.isPresent()
                ? ScopeType.LOCAL
                : ScopeType.GLOBAL;
    }
    
    public boolean isLoop() {
    	return ofLoop;
    }
    
    /**
     * Returns the scope that directly contains a given variable
     * Asserts that the variable must actually be present
     * 
     * @param variable - the variable you want to access
     */
    public Scope getLocation(Variable variable){
    	checkState(isInScope(variable));
    	if(isInScopeImmediately(variable)){
    		// It must be in this scope, so just return it
    		return this;
    	} else {
    		// Since the variable must be in scope
    		// this recursion will always succeed because you must find
    		// the variable before you run out of scopes
    		return parent.get().getLocation(variable);
    	}
    }

    /** Get the offset of a variable from the base pointer.
     * 
     * <p>The base pointer only moves when a function is invoked. So, finding
     * the offset of a variable, given its scope, requires looking at the parent
     * scopes.
     * 
     * <p>Requires that this is a local scope, or a parameter scope.  If it's a parameter scope,
     * we just return zero.  (This is only to provide a simple base case for the recursion through
     * locals.
     *  
     * @param variable
     * @return
     */
    public long offsetFromBasePointer(Variable variable) {
        if (getScopeType().equals(ScopeType.PARAMETER)) {
            return 0;
        }
        checkState(getScopeType().equals(ScopeType.LOCAL));
        if (isInScopeImmediately(variable)) {
            long sizeAdjustment = getFromScope(variable).get().getSize();
            return offset(variable) + effectiveBasePointerOffset() + sizeAdjustment;
        } else {
            // Recurse towards the global scope.
            // If we take this branch, we're not in the global scope, because
            // that would correspond to a not-found variable. The semantic
            // checks guarantee that the variable will be found.
            // As a result, we're in non-global scope, so the get() call will
            // succeed.
            return parent.get().offsetFromBasePointer(variable);
        }
    }
    
    /** Get the offset of a variable in the parameter set in the stack.
     * 
     * <p>Parameter variable are in the stack iff they are parameter 7 or greater,
     * thus parameters 1-6 have negative values because they are actually in registers.
     * 
     * <p>Requires that this is a parameter scope.
     *  
     * @param variable
     * @return
     */
    public long offsetInParameterSet(Variable variable) {
    	checkState(getScopeType().equals(ScopeType.PARAMETER));
        return (offset(variable) - 6);
    }

    /**
     * Get the offset of a variable, in this scope.
     * 
     * <p>The offset is the number of 64-bit spaces between the scope's base
     * and the variable.
     * 
     * <p>Requires that the variable is in this immediate scope.
     */
    public long offset(Variable variable) {
        long offset = 0;
        for (FieldDescriptor field : entries) {
            if (variable.equals(field.variable)) {
                return isLoop()
                		? offset + Architecture.LOOP_VAR_SIZE
                		: offset;
            }
            offset += field.getSize();
        }
        throw new AssertionError("Could not find " + variable + " in scope " + this);
    }

    private long effectiveBasePointerOffset() {
        if (!parent.isPresent()) {
            return 0;
        }
        Scope parentScope = parent.get();
        long parentBase = parentScope.effectiveBasePointerOffset();
        long parentExtent = parentScope.getScopeType() == ScopeType.LOCAL
                ? parentScope.size() 
                : 0; // Parameters come before the base pointer.
        return parentBase + parentExtent;
    }

    /**
     * Get the size of the scope. 
     *
     * <p>For locals this is the size of this scope's sub-frame on the stack.
     */
    public long size() {
        int offset = 0;
        for (FieldDescriptor field : entries) {
            offset += field.getSize();
        }
        return isLoop()
        		? offset + Architecture.LOOP_VAR_SIZE
        		: offset;
    }
    /**
     * Returns if possible the FieldDescriptor specifying the desired variable
     * if it does not exist, an empty optional is returned instead.
     * 
     * @param variable - the target variable
     * @return
     */
	public Optional<FieldDescriptor> getFromScope(Variable variable){
		if(isInScope(variable)){
			for(FieldDescriptor var: entries){
				if(variable.equals(var.variable))
					return Optional.of(var);
			}
			if(parent.isPresent())
				return parent.get().getFromScope(variable);
		}
		return Optional.<FieldDescriptor>absent();
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result
                + ((entries == null) ? 0 : entries.hashCode());
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
        if (!(obj instanceof Scope)) {
            return false;
        }
        Scope other = (Scope) obj;
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        if (entries == null) {
            if (other.entries != null) {
                return false;
            }
        } else if (!entries.equals(other.entries)) {
            return false;
        }
        return true;
    }

}
