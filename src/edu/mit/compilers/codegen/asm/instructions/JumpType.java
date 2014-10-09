package edu.mit.compilers.codegen.asm.instructions;

/**
 * Enumeration of jumps necessary for conditionals
 */
public enum JumpType {
    JZ ("JZ"), // zero
    JNZ ("JNZ"), // not zero
    JS ("JS"), // negative
    JNS ("JNS"), // not negative
    JO ("JO"), // arithmetic overflow
    JNO ("JNO"), // not overflow
    JL ("JL"), // signed less than
    JLE ("JLE"), // signed less than or equal
    JGE ("JGE"), // signed greater than or equal
    JG ("JG"); // signed greater than

    private String jumpName;

    private JumpType(String jumpName) {
        this.jumpName = jumpName;
    }

    public String toString() {
        return this.jumpName;
    }
}
