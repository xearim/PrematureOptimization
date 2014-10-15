package edu.mit.compilers.codegen.asm.instructions;

/**
 * Enumeration of jumps necessary for conditionals
 */
public enum JumpType {
    JZ("jz"), // zero
    JNZ("jnz"), // not zero
    JNE("jne"), // not equal
    JS("js"), // negative
    JNS("jns"), // not negative
    JO("jo"), // arithmetic overflow
    JNO("jno"), // not overflow
    JL("jl"), // signed less than
    JLE("jle"), // signed less than or equal
    JGE("jge"), // signed greater than or equal
    JG("jg"); // signed greater than
    
    String name;
    
    JumpType(String name){
    	this.name = name;
    }
    
    public String getName(){
    	return name;
    }
}
