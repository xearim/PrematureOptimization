package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Scope {
	
	private Optional<Scope> parent;
	private final ImmutableList<FieldDescriptor> variables;
	
	public Scope(List<FieldDescriptor> variables) {
        this.variables = ImmutableList.copyOf(variables);
        this.parent = Optional.<Scope>absent();
    }
	
	public Scope(List<FieldDescriptor> variables, Scope parent) {
        this.variables = ImmutableList.copyOf(variables);
        this.parent = Optional.of(parent);
    }
	
    public Optional<Scope> getParent() {
        return parent;
    }

	public void setParent(Scope parent){
		this.parent = Optional.of(parent);
	}
	
	public ImmutableList<FieldDescriptor> getVariables() {
		return variables;
	}
	/**
	 * Checks to see if a given identifier corresponds to an actually initialized variable
	 * visible from this scope looking upwards
	 * 
	 * @param identifier - the String identifier of the target variable
	 */
	public boolean isInScope(String identifier){
        for (FieldDescriptor var : variables) {
			if(identifier.equals(var.name))
				return true;
		}
		return (parent.isPresent() && parent.get().isInScope(identifier));
	}
	
	    /**
     * Return whether this scope has an entry for the given variable name. 
     *
     * <p>This method ignores any parent scopes. 
     */
    private boolean isInScopeImmediately(String variableName) {
        for (FieldDescriptor field : variables) {
            if (variableName.equals(field.name)) {
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

    /** Get the offset of a variable from the base pointer.
     * 
     * <p>The base pointer only moves when a function is invoked. So, finding
     * the offset of a variable, given its scope, requires looking at the parent
     * scopes.
     * 
     * <p>Requires that this is a local scope.
     *  
     * @param variableName
     * @return
     */
    public long offsetFromBasePointer(String variableName) {
        checkState(getScopeType().equals(ScopeType.LOCAL));
        if (isInScopeImmediately(variableName)) {
            return offset(variableName) + effectiveBasePointerOffset();
        } else {
            // Recurse towards the global scope.
            // If we take this branch, we're not in the global scope, because
            // that would correspond to a not-found variable. The semantic
            // checks guarantee that the variable will be found.
            // As a result, we're in non-global scope, so the get() call will
            // succeed.
            return parent.get().offsetFromBasePointer(variableName);
        }
    }

    /**
     * Get the offset of a variable, in this scope.
     * 
     * <p>The offset is the number of 64-bit spaces between the scope's base
     * and the variable.
     * 
     * <p>Requires that the variable is in this immediate scope.
     */
    public long offset(String variableName) {
        long offset = 0;
        for (FieldDescriptor field : variables) {
            if (variableName.equals(field.name)) {
                return offset;
            }
            offset += field.getSize();
        }
        throw new AssertionError("Could not find " + variableName + " in scope " + this);
    }

    public long effectiveBasePointerOffset() {
        switch (getScopeType()) {
            case GLOBAL:
                return 0;
            case LOCAL:
                Scope parentScope = parent.get();
                long parentBase = parentScope.effectiveBasePointerOffset();
                long parentExtent = parentScope.getScopeType() == ScopeType.PARAMETER
                        ? 0 // Parameters come before the base pointer.
                        : parentScope.size();
                return parentBase + parentExtent;
            case PARAMETER:
                // TODO(jasonpr): Decide if this is crazy.
                return 0;
            default:
                throw new AssertionError("Unexpected scope type for " + this);
        }
    }

    /**
     * Get the size of the scope. 
     *
     * <p>For locals this is the size of this scope's sub-frame on the stack.
     */
    public long size() {
        int offset = 0;
        for (FieldDescriptor field : variables) {
            offset += field.getSize();
        }
        return offset;
    }
    /**
     * Returns if possible the FieldDescriptor specifying the desired variable
     * if it does not exist, an empty optional is returned instead.
     * 
     * @param identifier - the String identifier of the target variable
     * @return
     */
	public Optional<FieldDescriptor> getFromScope(String identifier){
		if(isInScope(identifier)){
			for(FieldDescriptor var: variables){
				if(identifier.equals(var.name))
					return Optional.of(var);
			}
			if(parent.isPresent())
				return parent.get().getFromScope(identifier);
		}
		return Optional.<FieldDescriptor>absent();
	}

}
