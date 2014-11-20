package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

public class StringLiteralGraphFactory implements GraphFactory {
    private final StringLiteral stringLiteral;

    public StringLiteralGraphFactory(StringLiteral stringLiteral) {
        this.stringLiteral = stringLiteral;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        Label stringLabel = new Label(LabelType.STRING,
                Variable.forCompiler(stringLiteral.getID()));
        return BasicFlowGraph.<Instruction>builder()
                .append(push(stringLabel))
                .build();
    }
}