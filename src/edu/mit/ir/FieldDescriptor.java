package edu.mit.ir;

public class FieldDescriptor {
	
	final String name;
	final int length;
	final int lineNumber;
	final int columnNumber;
	final DataType type;
	
	// TODO assert that no FieldDescriptor has DataType void
	public FieldDescriptor(String name, int length, int lineNumber, int columnNumber, DataType type){
		this.name = name;
		this.length = length;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.type = type;
	}
	
}
