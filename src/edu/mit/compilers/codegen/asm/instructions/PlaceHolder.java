package edu.mit.compilers.codegen.asm.instructions;

public class PlaceHolder implements Instruction {

    private final String annotation;

    /** A placeholder for not-yet-implemented instructions. */
    public PlaceHolder(String annotation) {
        this.annotation = annotation;
    }

    @Override
    public String inAttSyntax() {
        return "NOP ;" + annotation;
    }

}
