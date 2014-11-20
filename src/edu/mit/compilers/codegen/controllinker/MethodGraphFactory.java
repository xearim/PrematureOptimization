package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.instructions.Instructions.enter;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.leave;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.ret;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;

/**
 * Produce a BiTerminalGraph that represents the entire execution of a method.
 *
 * <p>This produces a BiTerminalGrpah, not a ControlTerminalGraph.  Instead of, say,
 * exposing a "return" node, it just connects the return statement to the correct
 * instructions near the end of the BiTerminalGraph.
 */
public class MethodGraphFactory {

    private final FlowGraph<Instruction> graph;

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
    public MethodGraphFactory(BcrFlowGraph<ScopedStatement> methodDataFlowGraph,
            String name, boolean isVoid, long entriesToAllocate) {
        this.graph = calculateGraph(methodDataFlowGraph, name, isVoid, entriesToAllocate);
    }

    private FlowGraph<Instruction> calculateGraph(BcrFlowGraph<ScopedStatement> methodDataFlowGraph,
            String name, boolean isVoid, long entriesToAllocate) {
        // If we are the main method, we need to write down the base pointer for error handling
        boolean isMain = name.equals(Architecture.MAIN_METHOD_NAME);
        
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();
        
        // Entry code.
        builder.append(enter(entriesToAllocate));
        if (isMain){
            builder.append(move(Register.RBP, Architecture.MAIN_BASE_POINTER_ERROR_VARIABLE));
        }
        
        // Block graph.
        BcrFlowGraph<Instruction> blockGraph =
                DataFlowToControlFlowConverter.convert(methodDataFlowGraph);
        builder.append(blockGraph);

        // Fall Through Checking.
        if (!isVoid) {
            builder.append(new ErrorExitGraphFactory(Literal.CONTROL_DROP_OFF_EXIT).getGraph());
        }

        builder.setEndToSinkFor(builder.getEnd(), blockGraph.getReturnTerminal())
                .append(leave())
                .append(ret());

        // TODO(jasonpr): Connect break and continue to an explicit error thrower.
        // The semantic checker should ensure that we'll never have one in a method's block,
        // but... just in case.
        return builder.build();
    }

    public FlowGraph<Instruction> getGraph() {
        return graph;
    }
}
