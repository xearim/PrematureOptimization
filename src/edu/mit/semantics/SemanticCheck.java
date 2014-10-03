package edu.mit.semantics;

import java.util.List;

import edu.mit.compilers.ast.Program;
import edu.mit.semantics.errors.SemanticError;

@SuppressWarnings("unused")
public interface SemanticCheck {
	public List<SemanticError> doCheck();
}
