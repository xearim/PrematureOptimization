package edu.mit.compilers.codegen.controllinker;

import java.util.Map;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

/** A GraphFactory that delegates to any NativeExpression's custom factory. */
public class NativeExprGraphFactory implements GraphFactory {

    private final NativeExpression expr;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public NativeExprGraphFactory(NativeExpression expr, Scope scope, Map<ScopedVariable, Register> allocations) {
        this.expr = expr;
        this.scope = scope;
        this.allocations = allocations;
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return getDelegate().getGraph();
    }

    private GraphFactory getDelegate() {
        if (expr instanceof BinaryOperation) {
            return new BinOpGraphFactory((BinaryOperation) expr, scope, allocations);
        } else if (expr instanceof MethodCall) {
            return new MethodCallGraphFactory((MethodCall) expr, scope, allocations);
        } else if (expr instanceof TernaryOperation) {
            return new TernaryOpGraphFactory((TernaryOperation) expr, scope, allocations);
        } else if (expr instanceof UnaryOperation) {
            return new UnaryOpGraphFactory((UnaryOperation) expr, scope, allocations);
        } else if (expr instanceof Location) {
            return new VariableLoadGraphFactory((Location) expr, scope, allocations);
        } else if (expr instanceof NativeLiteral) {
            return new NativeLiteralGraphFactory((NativeLiteral) expr);
        } else {
            throw new AssertionError("Unexpeced NativeExpression: " + expr);
        }
    }
}
