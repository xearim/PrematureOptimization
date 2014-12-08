package edu.mit.compilers.codegen.controllinker;

import java.util.Map;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.codegen.controllinker.statements.CompareGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

public class TernaryOpGraphFactory implements GraphFactory {

    private final TernaryOperation operation;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public TernaryOpGraphFactory(TernaryOperation operation, Scope scope,
            Map<ScopedVariable, Register> allocations) {
        this.operation = operation;
        this.scope = scope;
        this.allocations = allocations;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        FlowGraph<Instruction> comparison =
                new CompareGraphFactory(operation.getCondition(), scope, allocations).getGraph();
        FlowGraph<Instruction> trueBranch =
                new NativeExprGraphFactory(operation.getTrueResult(), scope, allocations).getGraph();
        FlowGraph<Instruction> falseBranch =
                new NativeExprGraphFactory(operation.getFalseResult(), scope, allocations).getGraph();

        return BasicFlowGraph.<Instruction>builder()
                .append(comparison)
                .linkNonJumpBranch(comparison.getEnd(), trueBranch)
                .linkJumpBranch(comparison.getEnd(), JumpType.JNE, falseBranch)
                .setEndToSinkFor(trueBranch.getEnd(), falseBranch.getEnd())
                .build();
    }
}
