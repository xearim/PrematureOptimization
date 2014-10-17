package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.codegen.asm.Literal;

/**
 * Represents constant values of boolean, int or char type.
 */
public class NativeLiteralGraphFactory implements GraphFactory {
    private final TerminaledGraph graph;

    /*
     * Scope is not necessary since this is a constant
     */
    public NativeLiteralGraphFactory(NativeLiteral nl) {
        this.graph = calculateNativeLiteral(nl);
    }

    /**
     * Checks for which subclass it is and passes it on to other functions to
     * handle appropriately.
     */
    public TerminaledGraph calculateNativeLiteral(NativeLiteral nl) {
        return TerminaledGraph.ofInstructions(
                push(new Literal(nl.get64BitValue())));
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }

}