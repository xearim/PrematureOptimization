package edu.mit.ir;

import java.util.Map;

public class IntermediateRepresentation {

	// TODO(jasonpr) make map immutable
	private final Map<String, MethodDescriptor> methodTable;
	private final Scope global;
	
	public IntermediateRepresentation(Map<String, MethodDescriptor> methodTable, Scope global){
		this.methodTable = methodTable;
		this.global = global;
	}
}
