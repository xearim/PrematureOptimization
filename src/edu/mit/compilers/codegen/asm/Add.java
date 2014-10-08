package edu.mit.compilers.codegen.asm;

/**
 * A representation of the x86 ADD instruction.
 * 
 * <p>See Intel documentation for the spec. 
  */
public class Add implements Instruction {
    private final Value leftArgument;
    private final Value rightArgument;
    
    public Add(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
    
}
