package edu.mit.compilers.codegen.asm;

public class Register implements Value {

    public static final Register RAX = new Register(RegisterId.RAX);
    public static final Register RBX = new Register(RegisterId.RBX);
    public static final Register RDI = new Register(RegisterId.RDI);
    public static final Register RSI = new Register(RegisterId.RSI);
    public static final Register RDX = new Register(RegisterId.RDX);
    public static final Register RCX = new Register(RegisterId.RCX);
    public static final Register R8 = new Register(RegisterId.R8);
    public static final Register R9 = new Register(RegisterId.R9);
    public static final Register R10 = new Register(RegisterId.R10);
    public static final Register R11 = new Register(RegisterId.R11);
    public static final Register R12 = new Register(RegisterId.R12);
    public static final Register R13 = new Register(RegisterId.R13);
    public static final Register R14 = new Register(RegisterId.R14);
    public static final Register R15 = new Register(RegisterId.R15);
    public static final Register RSP = new Register(RegisterId.RSP);
    public static final Register RBP = new Register(RegisterId.RBP);

    private final RegisterId id;

    private static enum RegisterId {
        RAX("rax"),
        RBX("rbx"),
        RCX("rcx"),
        RDX("rdx"),
        RDI("rdi"),
        RSI("rsi"),
        R8("r8"),
        R9("r9"),
        R10("r10"),
        R11("r11"),
        R12("r12"),
        R13("r13"),
        R14("r14"),
        R15("r15"),
        RSP("rsp"),
        RBP("rbp");

        String name;

        RegisterId(String name){
        	this.name = name;
        }

        public String getName(){
        	return this.name;
        }
    }

    // Private constructor: The only way to get a reference to a Register
    // is through the public static instances.
    private Register(RegisterId id) {
        this.id = id;
    }

    @Override
    public String inAttSyntax() {
        return "%" + this.id.getName();
    }

    @Override
    public String toString() {
        return inAttSyntax();
    }
}
