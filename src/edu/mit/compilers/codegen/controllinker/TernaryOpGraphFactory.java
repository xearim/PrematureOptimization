package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.statements.CompareGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

public class TernaryOpGraphFactory implements GraphFactory {

    private final TernaryOperation operation;
    private final Scope scope;

    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope) {
        this.operation = operation;
        this.scope = scope;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        FlowGraph<Instruction> comparison =
                new CompareGraphFactory(operation.getCondition(), scope).getGraph();
        FlowGraph<Instruction> trueBranch =
                new NativeExprGraphFactory(operation.getTrueResult(), scope).getGraph();
        FlowGraph<Instruction> falseBranch =
                new NativeExprGraphFactory(operation.getFalseResult(), scope).getGraph();

        return BasicFlowGraph.<Instruction>builder()
                .append(comparison)
                .linkNonJumpBranch(comparison.getEnd(), trueBranch)
                .linkJumpBranch(comparison.getEnd(), JumpType.JNE, falseBranch)
                .setEndToSinkFor(trueBranch.getEnd(), falseBranch.getEnd())
                .build();
    }
}
