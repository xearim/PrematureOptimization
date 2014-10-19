package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.errorExit;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.enter;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.leave;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.ControlFlowNode;

/**
 * Produce a BiTerminalGraph that represents the entire execution of a method.
 *
 * <p>This produces a BiTerminalGrpah, not a ControlTerminalGraph.  Instead of, say,
 * exposing a "return" node, it just connects the return statement to the correct
 * instructions near the end of the BiTerminalGraph.
 */
public class MethodGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;

    public MethodGraphFactory(Method method) {
        this.graph = calculateGraph(method);
    }

    private BiTerminalGraph calculateGraph(Method method) {
        Block block = method.getBlock();

        
        BiTerminalGraph enterInstruction = BiTerminalGraph.ofInstructions(enter(block));
        ControlTerminalGraph blockGraph = new BlockGraphFactory(block).getGraph();
        // Link the Entry instruction to the start of the block
        enterInstruction.getEnd().setNext(blockGraph.getBeginning());

        BiTerminalGraph returnInstruction = BiTerminalGraph.ofInstructions(
        										leave(),
        										ret());
        ControlFlowNode sink = returnInstruction.getBeginning();

        boolean isVoid = !method.getReturnType().getReturnType().isPresent();
        BiTerminalGraph fallThroughChecker = isVoid
                ? BiTerminalGraph.ofInstructions()
                : BiTerminalGraph.ofInstructions(errorExit());

        // TODO(jasonpr): Connect break and continue to an explicit error thrower.
        // The semantic checker should ensure that we'll never have one in a method's block,
        // but... just in case.
        blockGraph.getControlNodes().getReturnNode().setNext(sink);
        blockGraph.getEnd().setNext(fallThroughChecker.getBeginning());
        fallThroughChecker.getEnd().setNext(sink);

        return new BiTerminalGraph(enterInstruction.getBeginning(), returnInstruction.getEnd());
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
