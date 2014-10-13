package edu.mit.compilers.ast;

/** A Decaf literal.  Namely, not a string literal. */
public interface NativeLiteral extends NativeExpression {
    /**
     * Get a representation of this literal as a 64-bit value, according to a per-implementation
     * specification.
     */
    public long get64BitValue();
}
