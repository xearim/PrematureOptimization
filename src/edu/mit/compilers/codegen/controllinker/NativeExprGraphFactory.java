package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class NativeExprGraphFactory implements GraphFactory {

    private final NativeExpression expr;

    public NativeExprGraphFactory(NativeExpression expr) {
        this.expr = expr;
    }

    @Override
    public TerminaledGraph getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof BinaryOperation) {
            return new BinOpGraphFactory((BinaryOperation) expr);
        } else if (expr instanceof MethodCall) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else if (expr instanceof TernaryOperation) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else if (expr instanceof UnaryOperation) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else if (expr instanceof Location) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else if (expr instanceof NativeLiteral) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
        } else {
            throw new AssertionError("Unexpeced NativeExpression: " + expr);
        }
    }
}
