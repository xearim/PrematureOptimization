package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.NativeLiteral;

public class Literal implements Value {
    public static final Literal TRUE = new Literal(1);
    public static final Literal FALSE = new Literal(0);

    private final long value;

    public Literal(NativeLiteral value) {
        this(value.get64BitValue());
    }

    public Literal(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String inAttSyntax() {
        return "$" + Long.toString(value);
    }
}
