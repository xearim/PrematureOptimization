package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;

import java.util.Map;

import edu.mit.compilers.ast.Condition;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

public class CompareGraphFactory implements GraphFactory {

    private final NativeExpression comparison;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public CompareGraphFactory(NativeExpression comparison, Scope scope, Map<ScopedVariable, Register> allocations){
        this.comparison = comparison;
        this.scope = scope;
        this.allocations = allocations;
    }

    public CompareGraphFactory(Condition condition, Scope scope, Map<ScopedVariable, Register> allocations) {
        this(condition.getExpression(), scope, allocations);
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return BasicFlowGraph.<Instruction>builder()
                .append(new NativeExprGraphFactory(comparison, scope, allocations).getGraph())
                .append(pop(R10))
                .append(compareFlagged(R10, Literal.TRUE))
                .build();
    }
}
