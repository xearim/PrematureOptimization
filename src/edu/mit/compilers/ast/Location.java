package edu.mit.compilers.ast;

import edu.mit.compilers.common.Variable;

/** A location of a variable, possibly at an index of an array. */
public interface Location extends NativeExpression {
    public Variable getVariable();
}
