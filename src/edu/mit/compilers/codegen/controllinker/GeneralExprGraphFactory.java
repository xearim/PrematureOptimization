package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.FlowGraph;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class GeneralExprGraphFactory implements GraphFactory {

    private final GeneralExpression expr;
    private final Scope scope;

    public GeneralExprGraphFactory(GeneralExpression expr, Scope scope) {
        this.expr = expr;
        this.scope = scope;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof StringLiteral) {
            return new StringLiteralGraphFactory((StringLiteral) expr);
        } else if (expr instanceof NativeExpression) {
            return new NativeExprGraphFactory((NativeExpression) expr, scope);
        } else {
            throw new AssertionError("Unexpeced GeneralExpression: " + expr);
        }
    }
}
