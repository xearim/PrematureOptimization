package edu.mit.compilers.codegen.asm;

public class Register implements Value {

    public static final Register R10 = new Register(RegisterId.R10);
    public static final Register R11 = new Register(RegisterId.R11);

    private final RegisterId id;

    private static enum RegisterId {
        // TODO(manny): Add remaining registers.
        RAX, RBX, RCX, RDX, R10, R11;
    }

    // Private constructor: The only way to get a reference to a Register
    // is through the public static instances.
    private Register(RegisterId id) {
        this.id = id;
    }

    @Override
    public String inAttSyntax() {
        // TODO(manny): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
}
