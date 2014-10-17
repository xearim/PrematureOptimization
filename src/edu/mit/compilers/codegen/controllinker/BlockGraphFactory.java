package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Scope;

public class BlockGraphFactory implements ControlTerminalGraphFactory {

    private final Block block;
    private final Scope scope;
    private final boolean inLoop;

    public BlockGraphFactory(Block block, Scope scope, boolean inLoop) {
        this.block = block;
        this.scope = scope;
        this.inLoop = inLoop;
    }

    @Override
    public ControlTerminalGraph getGraph() {
        throw new RuntimeException("Not yet implemented.");
    }
}
