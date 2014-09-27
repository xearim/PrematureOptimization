package edu.mit.ir;

public class FieldDescriptor {
	
	final String name;
	final int length;
	final int lineNumber;
	final DataType type;
	
	// TODO assert that no FieldDescriptor has DataType void
	public FieldDescriptor(String name, int length, int lineNumber, DataType type){
		this.name = name;
		this.length = length;
		this.lineNumber = lineNumber;
		this.type = type;
	}
	
}
