package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StringLiteral;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class GeneralExprGraphFactory implements GraphFactory {

    private final GeneralExpression expr;
    private final Scope scope;

    public GeneralExprGraphFactory(GeneralExpression expr, Scope scope) {
        this.expr = expr;
        this.scope = scope;
    }

    @Override
    public BiTerminalGraph getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof StringLiteral) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else if (expr instanceof NativeExpression) {
            return new NativeExprGraphFactory((NativeExpression) expr, scope);
        } else {
            throw new AssertionError("Unexpeced GeneralExpression: " + expr);
        }
    }
}
