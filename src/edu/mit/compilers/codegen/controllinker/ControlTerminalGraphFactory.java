package edu.mit.compilers.codegen.controllinker;

/**
 * Something that makes a ControlTerminalGraph.
 *
 * <p>Most implementations will take an AST node as a constructor argument.
 */
public interface ControlTerminalGraphFactory {
    public ControlTerminalGraph getGraph();
}
