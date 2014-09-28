package edu.mit.ir;

public interface Node {
	
	
	// Ensure the bad thing cannot happen
	public boolean canReturnOtherThan(DataType type);
	
	// Check that the good thing is possible
	public boolean mustReturn(DataType type);
	
	public Scope getScope();
}
