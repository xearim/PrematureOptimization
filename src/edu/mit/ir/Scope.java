package edu.mit.ir;

import java.util.List;

public class Scope {

	// TODO(jasonpr) make this optional for global scope
	private final Scope parent;
	private final List<FieldDescriptor> fields;
	
	public Scope(Scope parent, List<FieldDescriptor> fields){
		// TODO(jasonpr) check if null
		this.parent = parent;
		this.fields = fields;
	}
}
