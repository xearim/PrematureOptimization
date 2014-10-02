package edu.mit.compilers.ast;

import com.google.common.base.Optional;


public class FieldDescriptor {
	
    final String name;
    final Optional<IntLiteral> length;
    final BaseType type;
    final LocationDescriptor locationDescriptor;

    public FieldDescriptor(String name, BaseType type, LocationDescriptor locationDescriptor) {
		this.name = name;
        this.length = Optional.<IntLiteral> absent();
		this.type = type;
		this.locationDescriptor = locationDescriptor;
	}
	
    public FieldDescriptor(String name, IntLiteral length,
            BaseType type, LocationDescriptor locationDescriptor) {
		this.name = name;
		this.length = Optional.of(length);
		this.type = type;
        this.locationDescriptor = locationDescriptor;
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
        return locationDescriptor.lineNo();
	}
	
	public int getColumnNumber(){
        return locationDescriptor.colNo();
	}
}
