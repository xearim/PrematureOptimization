package edu.mit.compilers.codegen.asm.instructions;

/*
 * I literally went through wikipedia and just noted which ones looked like
 * they would be useful
 *
 * http://en.wikipedia.org/wiki/X86_instruction_listings#Original_8086.2F8088_instructions
 */
// TODO(Manny): ask team for clarification on which ones are necessary
public enum InstructionType {
    ADD ("ADD"),
    AND ("AND"),
    CALL ("CALL"),
    // CMP comparison using SUB
    CMP ("CMP"),
    // DEC decrement by 1
    DEC ("DEC"),
    // DIV unsigned divide
    // Don't think we need this one
    // IDIV signed divide
    IDIV ("IDIV"),
    // IMUL signed multiply
    IMUL ("IMUL"),
    // INC increment by 1
    INC ("INC"),
    JMP ("JMP"),
    MOV ("MOV"),
    // MUL unsigned multiply
    // Don't think we need this one
    // NEG two's complement negation
    NEG ("NEG"),
    NOT ("NOT"),
    POP ("POP"),
    PUSH ("PUSH"),
    //PUSHF push FLAGS onto stack
    PUSHF ("PUSHF"),
    // SAL signed shift left
    SAL ("SAL"),
    // SAR signed shift right
    SAR ("SAR"),
    // SHL unsigned shift left
    SHL ("SHL"),
    // SHR unsigned shift right
    SHR ("SHR"),
    SUB ("SUB"),
    // TEST comparison using AND
    TEST ("TEST"),
    // XOR exclusive or
    XOR ("XOR");

    private String instructionName;

    private InstructionType(String instructionName) {
        this.instructionName = instructionName;
    }

    public String toString() {
        return this.instructionName;
    }
}
