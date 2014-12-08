package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.RAX;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;

import java.util.Map;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

/**
 * Makes some instructions that setup a return statement.
 *
 * <p>Note that these instructions do not direct control flow.  That is, they do
 * not include a RET instruction.  They only *setup* the return, by putting the
 * return value in the return register.
 */
public class ReturnStatementGraphFactory implements GraphFactory {
    private ReturnStatement rs;
    private Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public ReturnStatementGraphFactory(ReturnStatement rs, Scope scope, Map<ScopedVariable, Register> allocations) {
        this.rs = rs;
        this.scope = scope;
        this.allocations = allocations;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();
        Optional<NativeExpression> returnValue = rs.getValue();
        // If there is a return expression, evaluate it and move it into RAX.
        if (returnValue.isPresent()) {
            builder.append(new NativeExprGraphFactory(returnValue.get(), scope, allocations).getGraph())
                .append(pop(RAX));
        } else {
            builder.append(Instructions.move(new Literal(0), RAX));
        }
        return builder.build();
    }
}
