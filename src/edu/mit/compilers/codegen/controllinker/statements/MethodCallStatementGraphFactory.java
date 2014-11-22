package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.MethodCallGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

/** Makes some instructions that execute a method call. */
public class MethodCallStatementGraphFactory implements GraphFactory {
	private MethodCall call;
	private Scope scope;

    public MethodCallStatementGraphFactory(MethodCall call, Scope scope) {
        this.call = call;
        this.scope = scope;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return BasicFlowGraph.<Instruction>builder()
                .append(new MethodCallGraphFactory(call, scope).getGraph())
                // When we use a method call as a statement, we discard the return value.
                .append(add(new Literal(Architecture.WORD_SIZE), Register.RSP))
                .build();
    }
}
