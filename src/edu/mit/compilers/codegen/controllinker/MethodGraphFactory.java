package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Method;


public class MethodGraphFactory implements GraphFactory {

    private final Method method;

    public MethodGraphFactory(Method method) {
        this.method = method;
    }

    @Override
    public TerminaledGraph getGraph() {
        // TODO(jasonpr): Implement.
        throw new RuntimeException("Not yet implemented.");
    }
}
