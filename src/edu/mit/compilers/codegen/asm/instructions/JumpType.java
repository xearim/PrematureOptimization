package edu.mit.compilers.codegen.asm.instructions;

/**
 * Enumeration of jumps necessary for conditionals
 */
public enum JumpType {
    JZ, // zero
    JNZ, // not zero
    JS, // negative
    JNS, // not negative
    JO, // arithmetic overflow
    JNO, // not overflow
    JL, // signed less than
    JLE, // signed less than or equal
    JGE, // signed greater than or equal
    JG; // signed greater than
}
