package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import edu.mit.compilers.ast.BooleanLiteral;
import edu.mit.compilers.ast.CharLiteral;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.NativeLiteral;
import edu.mit.compilers.codegen.asm.Literal;

/**
 * Represents constant values of boolean, int or char type.
 *
 * TODO(Manny): Keep as NativeLiteral, just use get64BitValue
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
        if (nl instanceof BooleanLiteral) {
            return calculateBooleanLiteral((BooleanLiteral) nl);
        } else if (nl instanceof CharLiteral) {
            return calculateCharLiteral((CharLiteral) nl);
        } else if (nl instanceof IntLiteral) {
            return calculateIntLiteral((IntLiteral) nl);
        } else {
            throw new AssertionError("Unexpected NativeLiteral type with value: " + Long.toString(nl.get64BitValue()));
        }
    }

    private TerminaledGraph calculateBooleanLiteral(BooleanLiteral bl) {
        return TerminaledGraph.ofInstructions(
                push(new Literal(bl.get64BitValue())));
    }

    private TerminaledGraph calculateCharLiteral(CharLiteral cl) {
        return TerminaledGraph.ofInstructions(
                push(new Literal(cl.get64BitValue())));

    }

    private TerminaledGraph calculateIntLiteral(IntLiteral il) {
        return TerminaledGraph.ofInstructions(
                push(new Literal(il.get64BitValue())));
    }

    @Override
    public TerminaledGraph getGraph() {
        return graph;
    }

}
