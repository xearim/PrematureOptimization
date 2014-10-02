package edu.mit.semantics;

import java.util.List;

import edu.mit.semantics.errors.SemanticError;

public interface SemanticCheck {
	public List<SemanticError> doCheck();
}
