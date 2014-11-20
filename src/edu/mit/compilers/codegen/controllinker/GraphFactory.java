package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.FlowGraph;

/**
 * Something that makes a BiTerminalGraph.
 * 
 * <p>Most implementations will take an AST node as a constructor argument.
 */
public interface GraphFactory {
    public FlowGraph<Instruction> getGraph();
}
