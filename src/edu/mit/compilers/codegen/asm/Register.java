package edu.mit.compilers.codegen.asm;

public class Register implements Value {
    public static enum RegisterId {
        // TODO(manny): Add remaining registers.
        RAX, RBX, RCX, RDX, R10, R11;
    }

    @Override
    public String inAttSyntax() {
        // TODO(manny): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
    
    
}
