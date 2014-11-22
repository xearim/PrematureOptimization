package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

/**
 * Represents constant values of boolean, int or char type.
 */
public class NativeLiteralGraphFactory implements GraphFactory {
    private final NativeLiteral nativeLiteral;

    // No scope parameter is necessary, since this is a constant.
    public NativeLiteralGraphFactory(NativeLiteral nativeLiteral) {
        this.nativeLiteral = nativeLiteral;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return BasicFlowGraph.<Instruction>builder()
                .append(push(new Literal(nativeLiteral)))
                .build();
    }
}
