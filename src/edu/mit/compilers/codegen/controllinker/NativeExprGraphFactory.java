package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class NativeExprGraphFactory implements GraphFactory {

    private final NativeExpression expr;
    private final Scope scope;
    private final boolean inMethodCall;

    public NativeExprGraphFactory(NativeExpression expr, Scope scope, boolean inMethodCall) {
        this.expr = expr;
        this.scope = scope;
        this.inMethodCall = inMethodCall;
    }
    
    public NativeExprGraphFactory(NativeExpression expr, Scope scope){
    	this(expr, scope, false);
    }

    @Override
    public BiTerminalGraph getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof BinaryOperation) {
            return new BinOpGraphFactory((BinaryOperation) expr, scope, inMethodCall);
        } else if (expr instanceof MethodCall) {
            return new MethodCallGraphFactory((MethodCall) expr, scope);
        } else if (expr instanceof TernaryOperation) {
            return new TernaryOpGraphFactory((TernaryOperation) expr, scope, inMethodCall);
        } else if (expr instanceof UnaryOperation) {
            return new UnaryOpGraphFactory((UnaryOperation) expr, scope, inMethodCall);
        } else if (expr instanceof Location) {
            return new VariableLoadGraphFactory((Location) expr, scope, inMethodCall);
        } else if (expr instanceof NativeLiteral) {
            return new NativeLiteralGraphFactory((NativeLiteral) expr);
        } else {
            throw new AssertionError("Unexpeced NativeExpression: " + expr);
        }
    }
}
