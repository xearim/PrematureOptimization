package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R8;
import static edu.mit.compilers.codegen.asm.Register.R9;
import static edu.mit.compilers.codegen.asm.Register.RCX;
import static edu.mit.compilers.codegen.asm.Register.RDI;
import static edu.mit.compilers.codegen.asm.Register.RDX;
import static edu.mit.compilers.codegen.asm.Register.RSI;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

public class RegisterSaver {

    /**
     * The registers in which the caller stores the first six arguments for a funtion call,
     * in order.
     */
    public static final List<Register> PARAMETER_REGISTERS =
            ImmutableList.of(RDI, RSI, RDX, RCX, R8, R9);

    private RegisterSaver() {}

    /** Push all six registers that are used for method calls onto the stack. */
    public static FlowGraph<Instruction> pushAll() {
        BasicFlowGraph.Builder<Instruction> pusher = BasicFlowGraph.builder();
        for (Register register : PARAMETER_REGISTERS) {
            pusher.append(push(register));
        }
        return pusher.build();
    }

    /** Pop the argument registers, in the reverse order of #pushAll. */
    public static FlowGraph<Instruction> popAll() {
        BasicFlowGraph.Builder<Instruction> popper = BasicFlowGraph.builder();
        for (Register register : Lists.reverse(PARAMETER_REGISTERS)) {
            popper.append(pop(register));
        }
        return popper.build();
    }
}
