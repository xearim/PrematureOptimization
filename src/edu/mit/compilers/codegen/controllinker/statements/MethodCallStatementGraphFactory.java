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

    private final ControlTerminalGraph graph;

    public MethodCallStatementGraphFactory(MethodCall call, Scope scope) {
        this.graph = calculateGraph(call, scope);
    }

    private ControlTerminalGraph calculateGraph(MethodCall call, Scope scope) {
        BiTerminalGraph expressionGraph = new MethodCallGraphFactory(call, scope).getGraph();
        // When we use a method call as a statement, we discard the return value.
        BiTerminalGraph statementGraph = BiTerminalGraph.sequenceOf(
                expressionGraph,
                BiTerminalGraph.ofInstructions(
                        add(new Literal(Architecture.WORD_SIZE), Register.RSP)));
        return ControlTerminalGraph.ofBiTerminalGraph(statementGraph);
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}
