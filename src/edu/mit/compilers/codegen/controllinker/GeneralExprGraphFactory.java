package edu.mit.compilers.codegen.controllinker;

import java.util.Map;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class GeneralExprGraphFactory implements GraphFactory {

    private final GeneralExpression expr;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;
    
    public GeneralExprGraphFactory(GeneralExpression expr, Scope scope, Map<ScopedVariable, Register> allocations) {
        this.expr = expr;
        this.scope = scope;
        this.allocations = allocations;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof StringLiteral) {
            return new StringLiteralGraphFactory((StringLiteral) expr);
        } else if (expr instanceof NativeExpression) {
            return new NativeExprGraphFactory((NativeExpression) expr, scope, allocations);
        } else {
            throw new AssertionError("Unexpeced GeneralExpression: " + expr);
        }
    }
}
