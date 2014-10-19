package edu.mit.compilers.codegen.asm.instructions;

/*
 * I literally went through wikipedia and just noted which ones looked like
 * they would be useful
 *
 * http://en.wikipedia.org/wiki/X86_instruction_listings#Original_8086.2F8088_instructions
 */
// TODO(Manny): ask team for clarification on which ones are necessary
public enum InstructionType {
    CALL ("CALL"),
    POP ("POP"),
    PUSH ("PUSH"),
    // CMP comparison that sets flags
    CMPF ("CMPF"),
    // CMP comparison produces values
    CMP ("CMP"),
    AND ("AND"),
    OR ("OR"),
    // NEG two's complement negation
    NOT ("NOT"),
    // DEC decrement by 1
    DEC ("DEC"),
    // INC increment by 1
    INC ("INC"),
    ADD ("ADD"),
    SUB ("SUB"),
    NEG ("NEG"),
    // IDIV signed divide
    IDIV ("IDIV"),
    // IMUL signed multiply
    IMUL ("IMUL"),
    // MODULO, not a standard instr but we support it i guess
    MODULO ("MODULO"),
    // SHL shift left
    SHL ("SHL"),
    // SHR shift right
    SHR ("SHR"),
    // Unconditional Jump
    JMP ("JMP"),
    MOV ("MOV"),
    ENTER ("ENTER"),
    LEAVE ("LEAVE"),
    LABEL ("LABEL");

    private String instructionName;

    private InstructionType(String instructionName) {
        this.instructionName = instructionName;
    }

    public String toString() {
        return this.instructionName;
    }
}
