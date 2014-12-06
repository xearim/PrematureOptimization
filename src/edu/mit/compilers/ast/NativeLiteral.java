package edu.mit.compilers.ast;

/** A Decaf literal.  Namely, not a string literal. */
public abstract class NativeLiteral implements NativeExpression {
    /**
     * Get a representation of this literal as a 64-bit value, according to a per-implementation
     * specification.
     */
    public abstract long get64BitValue();

    @Override
    public final NativeExpression
            withReplacements(NativeExpression toReplace, NativeExpression replacement) {
        return this.equals(toReplace) ? replacement : this;
    }
}
