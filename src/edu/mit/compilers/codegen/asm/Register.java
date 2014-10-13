package edu.mit.compilers.codegen.asm;

public class Register implements Value {

    public static final Register RAX = new Register(RegisterId.RAX);
    public static final Register RDI = new Register(RegisterId.RDI);
    public static final Register RSI = new Register(RegisterId.RSI);
    public static final Register RDX = new Register(RegisterId.RDX);
    public static final Register RCX = new Register(RegisterId.RCX);
    public static final Register R8 = new Register(RegisterId.R8);
    public static final Register R9 = new Register(RegisterId.R9);
    public static final Register R10 = new Register(RegisterId.R10);
    public static final Register R11 = new Register(RegisterId.R11);
    public static final Register RSP = new Register(RegisterId.RSP);

    private final RegisterId id;

    private static enum RegisterId {
        // TODO(manny): Add remaining registers.
        RAX, RBX, RCX, RDX, RDI, RSI, R8, R9, R10, R11, RSP;
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
