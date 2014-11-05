package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.enter;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.leave;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.dataflow.DataFlow;

/**
 * Produce a BiTerminalGraph that represents the entire execution of a method.
 *
 * <p>This produces a BiTerminalGrpah, not a ControlTerminalGraph.  Instead of, say,
 * exposing a "return" node, it just connects the return statement to the correct
 * instructions near the end of the BiTerminalGraph.
 */
public class MethodGraphFactory implements GraphFactory {

    private final BiTerminalGraph graph;

    /**
     * Constructor.
     *
     * @param methodDataFlowGraph The data flow graph representing the method.
     * @param name The name of the method. (Currently only used to identify the special
     *      method 'main'.)
     * @param isVoid Whether the method has void return type.
     * @param entriesToAllocate How many quadwords of memory need to be allocated on the stack to
     *      hold the variables at and below the method's scope.
     */
    public MethodGraphFactory(
            DataFlow methodDataFlowGraph, String name, boolean isVoid, long entriesToAllocate) {
        this.graph = calculateGraph(methodDataFlowGraph, name, isVoid, entriesToAllocate);
    }

    private BiTerminalGraph calculateGraph(
            DataFlow methodDataFlowGraph, String name, boolean isVoid, long entriesToAllocate) {
        // If we are the main method, we need to write down the base pointer for error handling
        boolean isMain = name.equals(Architecture.MAIN_METHOD_NAME);
        BiTerminalGraph enterInstruction = isMain
                ? BiTerminalGraph.ofInstructions(
                        enter(entriesToAllocate),
                        move(Register.RBP, Architecture.MAIN_BASE_POINTER_ERROR_LABEL))
                : BiTerminalGraph.ofInstructions(enter(entriesToAllocate));

        //DataFlow test = new BlockDataFlowFactory(block).getDataFlow();				
        ControlTerminalGraph blockGraph = new DataFlowGraphFactory(
                methodDataFlowGraph).getGraph();
        //ControlTerminalGraph blockGraph = new BlockGraphFactory(block).getGraph();
        // Link the Entry instruction to the start of the block
        enterInstruction.getEnd().setNext(blockGraph.getBeginning());

        BiTerminalGraph returnInstruction = BiTerminalGraph.ofInstructions(
        										leave(),
        										ret());
        ControlFlowNode sink = returnInstruction.getBeginning();

        BiTerminalGraph fallThroughChecker = isVoid
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
