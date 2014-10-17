package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveFromArray;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.VariableReference;


public class VariableLoadGraphFactory implements GraphFactory {

    private final TerminaledGraph graph;

    public VariableLoadGraphFactory(Location location, Scope scope) {
        this.graph = calculateLoad(location, scope);
    }

    private TerminaledGraph calculateLoad(Location location, Scope scope) {
        if (location instanceof ArrayLocation) {
            return calculateLoadFromArray((ArrayLocation) location, scope);
        } else if (location instanceof ScalarLocation) {
            return calculateLoadFromScalar((ScalarLocation) location, scope);
        } else {
            throw new AssertionError("Unexpected location type for " + location);
        }
    }

    private TerminaledGraph calculateLoadFromArray(ArrayLocation location, Scope scope) {
        String name = location.getVariableName();
        NativeExpression index = location.getIndex();
        long bytesPerEntry = 8;

        return TerminaledGraph.sequenceOf(
                new NativeExprGraphFactory(index, scope).getGraph(),
                TerminaledGraph.ofInstructions(
                        pop(R10),
                        moveFromArray(new VariableReference(name, scope),
                                R10, bytesPerEntry, R11),
                        push(R11)));
    }

    private TerminaledGraph calculateLoadFromScalar(ScalarLocation location, Scope scope) {
        String name = location.getName();
        return TerminaledGraph.ofInstructions(
                move(new VariableReference(name, scope), R11),
                push(R11));
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }
}
