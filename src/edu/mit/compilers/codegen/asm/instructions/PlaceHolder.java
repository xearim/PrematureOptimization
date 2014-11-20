package edu.mit.compilers.codegen.asm.instructions;

public class PlaceHolder extends Instruction {

    private final String annotation;

    /** A placeholder for not-yet-implemented instructions. */
    public PlaceHolder(String annotation) {
        this.annotation = annotation;
    }

    @Override
    public String inAttSyntax() {
    	// Gonna supress this for now, just for testing
    	return "";
        //return "nop ;" + annotation;
    }

}
