package edu.mit.compilers.ast;

import com.google.common.base.Optional;


public class FieldDescriptor {
	
	final String name;
	final Optional<Integer> length;
	final int lineNumber;
	final int columnNumber;
	final BaseType type;
	
	public FieldDescriptor(String name, int lineNumber, int columnNumber, BaseType type){
		this.name = name;
		this.length = Optional.<Integer>absent();
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.type = type;
	}
	
	public FieldDescriptor(String name, int length, int lineNumber, int columnNumber, BaseType type){
		this.name = name;
		this.length = Optional.of(length);
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}
}
