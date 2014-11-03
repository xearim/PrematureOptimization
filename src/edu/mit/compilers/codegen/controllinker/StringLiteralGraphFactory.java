package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.common.Variable;

public class StringLiteralGraphFactory implements GraphFactory {
    private final BiTerminalGraph graph;
    private final Label stringLabel;

    public StringLiteralGraphFactory(StringLiteral stringLiteral) {
    	this.stringLabel = new Label(LabelType.STRING, Variable.forCompiler(stringLiteral.getID()));
        this.graph = calculateStringLiteral(stringLiteral);
    }
    
    public BiTerminalGraph calculateStringLiteral(StringLiteral stringLiteral){
    	return BiTerminalGraph.ofInstructions(
                push(stringLabel));
    }

	@Override
	public BiTerminalGraph getGraph() {
		return graph;
	}
}