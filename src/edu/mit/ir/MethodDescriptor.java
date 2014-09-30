package edu.mit.ir;

public class MethodDescriptor {
	
	private final Scope parameters;
	private final Block block;
	private int lineNumber;
	private final DataType returnType;
	
	public MethodDescriptor(Scope parameters, Block block, int lineNumber, DataType returnType){
		this.parameters = parameters;
		this.block = block;
		this.lineNumber = lineNumber;
		this.returnType = returnType;
	}
}
