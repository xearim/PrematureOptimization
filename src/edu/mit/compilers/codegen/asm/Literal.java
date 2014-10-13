package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.NativeLiteral;

public class Literal implements Value {
    private final NativeLiteral value;

    public Literal(NativeLiteral value) {
        this.value = value;
    }

    public long getValue() {
        return value.get64BitValue();
    }

    @Override
    public String inAttSyntax() {
        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
}
