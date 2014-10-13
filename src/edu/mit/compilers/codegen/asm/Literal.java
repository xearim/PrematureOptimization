package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.NativeLiteral;

public class Literal implements Value {
    public static final Literal TRUE = new Literal(1);

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
        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
}
