package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;
import edu.mit.compilers.codegen.controllinker.MethodCallGraphFactory;

public class MethodCallStatementGraphFactory implements ControlTerminalGraphFactory {
	private MethodCall call;
	private Scope scope;

    public MethodCallStatementGraphFactory(MethodCall call, Scope scope) {
        this.call = call;
        this.scope = scope;
    }

    private ControlTerminalGraph calculateGraph(MethodCall call, Scope scope) {
        BiTerminalGraph expressionGraph = new MethodCallGraphFactory(call, scope).getGraph();
        // When we use a method call as a statement, we discard the return value.
        // And we make sure to save everything before hand, and get it back afterwards
        BiTerminalGraph statementGraph = BiTerminalGraph.sequenceOf(
                expressionGraph,
                BiTerminalGraph.ofInstructions(
                        add(new Literal(Architecture.WORD_SIZE), Register.RSP)));
        return ControlTerminalGraph.ofBiTerminalGraph(statementGraph);
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return calculateGraph(call, scope);
    }
}
