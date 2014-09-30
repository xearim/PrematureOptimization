package edu.mit.ir;

public class Block {

	private final Scope scope;
	private final Statements statements;
	
	public Block(Scope scope, Statements statements){
		this.scope = scope;
		this.statements = statements;
	}
}
