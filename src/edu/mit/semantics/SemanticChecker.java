package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import edu.mit.compilers.ast.Callout;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.Node;
import edu.mit.compilers.ast.NodeSequence;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.Scope;
import edu.mit.semantics.errors.DeclaredTwiceSemanticError;
import edu.mit.semantics.errors.LocationInFile;
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
		
		return errors;
	}
}
