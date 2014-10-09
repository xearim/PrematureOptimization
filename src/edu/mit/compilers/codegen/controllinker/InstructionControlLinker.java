package edu.mit.compilers.codegen.controllinker;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.instructions.Instruction;

/** An immutable plan to sequentially execute some instructions. */
public class InstructionControlLinker implements ControlLinker{
    private final ImmutableList<Instruction> instructions;

    private InstructionControlLinker(List<Instruction> instructions) {
        this.instructions = ImmutableList.copyOf(instructions);
    }

    @Override
    public ControlFlowNode linkTo(ControlFlowNode sink) {
        ControlFlowNode head = sink;
        for (Instruction instr : Lists.reverse(instructions)) {
            head = SequentialControlFlowNode.WithNext(instr, head);
        }
        return head;
    }

    /** Make a plan to execute some instructions in the specified order. */
    public static InstructionControlLinker of(Instruction... instructions) {
        return new InstructionControlLinker(Arrays.asList(instructions));
    }
}
