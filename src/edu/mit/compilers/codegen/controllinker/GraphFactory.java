package edu.mit.compilers.codegen.controllinker;

/**
 * Something that makes a TerminaledGraph.
 * 
 * <p>Most implementations will take an AST node as a constructor argument.
 */
public interface GraphFactory {
    public TerminaledGraph getGraph();
}
