package edu.mit.ir;

import java.util.List;

public class Statements {

	private final List<Node> statementSequence;
	private final Scope scope;
	
	public Statements(List<Node> statementSequence, Scope scope){
		this.statementSequence = statementSequence;
		this.scope = scope;
	}
}
