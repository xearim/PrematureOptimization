package edu.mit.compilers.ast;

/** A location of a variable, possibly at an index of an array. */
public interface Location extends NativeExpression {
    public String getVariableName();
}
