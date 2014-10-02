package edu.mit.compilers.ast;

import com.google.common.base.Optional;


public class FieldDescriptor {
	
	final String name;
    final Optional<IntLiteral> length;
    final int lineNumber;
	final int columnNumber;
	final BaseType type;
	
    public FieldDescriptor(String name, int lineNumber, int columnNumber, BaseType type) {
		this.name = name;
        this.length = Optional.<IntLiteral> absent();
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.type = type;
	}
	
    public FieldDescriptor(String name, IntLiteral length, int lineNumber, int columnNumber,
            BaseType type) {
		this.name = name;
		this.length = Optional.of(length);
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.type = type;
	}
	
	public BaseType getType(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	public Optional<IntLiteral> getLength(){
		return length;
	}
	
	public int getLineNumber(){
		return lineNumber;
	}
	
	public int getColumnNumber(){
		return columnNumber;
	}
	
	
}
