package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;

public class Return extends Instruction {
    private InstructionType type = InstructionType.RET;

    public Return() {
    }

    @Override
    public String inAttSyntax() {
        // just call return
        return "ret ";
    }
}
