package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.ControlFlowNode;

/** A ControlLinker that delegates to any NativeExpression's custom linker. */
public class NativeExprLinker implements ControlLinker {

    private final NativeExpression expr;

    public NativeExprLinker(NativeExpression expr) {
        this.expr = expr;
    }

    @Override
    public ControlFlowNode linkTo(ControlFlowNode sink) {
        return getDelegate().linkTo(sink);
    }

    private ControlLinker getDelegate() {
        if (expr instanceof BinaryOperation) {
            // TODO(jasonpr): Implement.
            throw new RuntimeException("Not yet implemented.");
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
