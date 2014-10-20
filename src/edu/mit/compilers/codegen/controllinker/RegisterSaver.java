package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R8;
import static edu.mit.compilers.codegen.asm.Register.R9;
import static edu.mit.compilers.codegen.asm.Register.RCX;
import static edu.mit.compilers.codegen.asm.Register.RDI;
import static edu.mit.compilers.codegen.asm.Register.RDX;
import static edu.mit.compilers.codegen.asm.Register.RSI;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

public class RegisterSaver {

    private RegisterSaver() {}

    // TODO(jasonpr): Do something less bug-prone than specifying these registers twice.
    /** Push all six registers that are used for method calls onto the stack. */
    public static BiTerminalGraph pushAll() {
        return BiTerminalGraph.ofInstructions(
                push(RDI), push(RSI), push(RDX), push(RCX), push(R8), push(R9));
    }

    /** Pop the argument registers, in the reverse order of #pushAll. */
    public static BiTerminalGraph popAll() {
        return BiTerminalGraph.ofInstructions(
                pop(R9), pop(R8), pop(RCX), pop(RDX), pop(RSI), pop(RDI));
    }
}
