package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ast.Program;
import edu.mit.semantics.errors.SemanticError;

public class SemanticChecker {
	private Program prog;
	private List<SemanticError> errors = new ArrayList<SemanticError>();
	
	public SemanticChecker(Program ir) {
		this.prog = ir;
	}
	
	public List<SemanticError> checkProgram() {
		/*
		 * Implement checks
		 */
		errors.addAll(new DeclaredTwiceSemanticCheck(this.prog).doCheck()); // 1
		errors.addAll(new MissingMainSemanticCheck(this.prog).doCheck()); // 3
		
		return errors;
	}
}
