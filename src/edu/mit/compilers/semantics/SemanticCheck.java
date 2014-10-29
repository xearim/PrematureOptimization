package edu.mit.compilers.semantics;

import java.util.List;

import edu.mit.compilers.ast.Program;
import edu.mit.compilers.semantics.errors.SemanticError;

@SuppressWarnings("unused")
public interface SemanticCheck {
	public List<SemanticError> doCheck();
}
