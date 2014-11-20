package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

public class CompareGraphFactory implements GraphFactory {

    private final NativeExpression comparison;
    private final Scope scope;

    public CompareGraphFactory(NativeExpression comparison, Scope scope){
        this.comparison = comparison;
        this.scope = scope;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(comparison, scope).getGraph())
                .append(pop(R10))
                .append(compareFlagged(R10, Literal.TRUE))
                .build();
    }
}
