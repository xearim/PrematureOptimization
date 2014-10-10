package edu.mit.compilers.codegen.asm;

// TODO(jasonpr): Figure out if we want to separate int and boolean. 
public class Literal implements Value {
    private final long value;

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
