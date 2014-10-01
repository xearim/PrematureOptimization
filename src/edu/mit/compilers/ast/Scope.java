package edu.mit.compilers.ast;

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
	
	public void setParent(Scope parent){
		this.parent = Optional.of(parent);
	}
	
	/**
	 * Checks to see if a given identifier corresponds to an actually initialized variable
	 * visible from this scope looking upwards
	 * 
	 * @param identifier - the String identifier of the target variable
	 */
	public boolean isInScope(String identifier){
		for(FieldDescriptor var: variables){
			if(identifier.equals(var.name))
				return true;
		}
		return (parent.isPresent() && parent.get().isInScope(identifier));
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
