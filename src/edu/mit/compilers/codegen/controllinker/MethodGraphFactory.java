package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.enter;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.leave;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;

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

        // If we are the main method, we need to write down the base pointer for error handling
        BiTerminalGraph enterInstruction = method.isMain()
        		? BiTerminalGraph.ofInstructions(
        				enter(block),
        				move(Register.RBP, Architecture.MAIN_BASE_POINTER_ERROR_VARIABLE))
        		: BiTerminalGraph.ofInstructions(enter(block));
        ControlTerminalGraph blockGraph = new BlockGraphFactory(block).getGraph();
        // Link the Entry instruction to the start of the block
        enterInstruction.getEnd().setNext(blockGraph.getBeginning());

        BiTerminalGraph returnInstruction = BiTerminalGraph.ofInstructions(
        										leave(),
        										ret());
        ControlFlowNode sink = returnInstruction.getBeginning();

        BiTerminalGraph fallThroughChecker = method.isVoid()
                ? BiTerminalGraph.ofInstructions()
                : new ErrorExitGraphFactory(Literal.CONTROL_DROP_OFF_EXIT).getGraph();

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
