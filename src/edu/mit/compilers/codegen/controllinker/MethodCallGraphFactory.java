package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R8;
import static edu.mit.compilers.codegen.asm.Register.R9;
import static edu.mit.compilers.codegen.asm.Register.RAX;
import static edu.mit.compilers.codegen.asm.Register.RCX;
import static edu.mit.compilers.codegen.asm.Register.RDI;
import static edu.mit.compilers.codegen.asm.Register.RDX;
import static edu.mit.compilers.codegen.asm.Register.RSI;
import static edu.mit.compilers.codegen.asm.Register.RSP;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.call;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.subtract;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;

/**
 * A GraphFactory that calls a method or callout using the GCC calling convention.
 *
 * <p>Always pushes a result to the stack, EVEN IF THE RETURN TYPE IS VOID.  (If the return
 * type is void, the result's value is unspecified.)
 *
 * <p>It is the caller's responsibility to pop the return value from the stack if it is unneeded,
 * i.e. if the function is only called for its side effects.
 */
public class MethodCallGraphFactory implements GraphFactory {
    /**
     * The registers in which to put the first six parameters. See
     * http://wiki.osdev.org/Calling_Conventions.
     */
    private static final List<Register> ARG_REGISTERS =
            ImmutableList.of(RDI, RSI, RDX, RCX, R8, R9);

    private final BiTerminalGraph graph;

    public MethodCallGraphFactory(MethodCall methodCall, Scope scope) {
        this.graph = calculateGraph(methodCall, scope);
    }

    private BiTerminalGraph calculateGraph(MethodCall methodCall, Scope scope) {
        List<GeneralExpression> args = ImmutableList.copyOf(methodCall.getParameterValues());

        SequentialControlFlowNode preCallStart = SequentialControlFlowNode.namedNop(methodCall.getMethodName() + "()");
        
        // We need to store the extra arguments above the stored registers
        int numOverflowingArgs = args.size() > ARG_REGISTERS.size()
        						? args.size() - ARG_REGISTERS.size()
        						: 0;
        BiTerminalGraph offsetStack = BiTerminalGraph.ofInstructions(subtract(new Literal(args.size() * Architecture.WORD_SIZE), RSP));
        preCallStart.setNext(offsetStack.getBeginning());
        
        SequentialControlFlowNode preCallEnd = offsetStack.getEnd();
        
        // Setup args for call.
        int argNumber = 0;
        while (argNumber < args.size()) {
            BiTerminalGraph argEvaluator =
                    new GeneralExprGraphFactory(args.get(argNumber), scope).getGraph();

            // Once it's on the stack, we leave it there for the function call.
            BiTerminalGraph argSetup = BiTerminalGraph.sequenceOf(argEvaluator,
                				BiTerminalGraph.ofInstructions(
                				pop(Register.R10),
                				move(Register.R10, 
                					new Location(RSP, (argNumber)*Architecture.BYTES_PER_ENTRY))
                					));

            // Hook this arg setup into the graph.
            preCallEnd.setNext(argSetup.getBeginning());
            preCallEnd = argSetup.getEnd();

            argNumber++;
        }
        
        // Need to save Arg registers before method call
        BiTerminalGraph saveArgRegisters = RegisterSaver.pushAll();
        preCallEnd.setNext(saveArgRegisters.getBeginning());
        preCallEnd = saveArgRegisters.getEnd();
        
        // Now we need to move the guys we stashed above to their final resting spots
        argNumber = args.size() - 1;
        int offset = 0;
        while (argNumber >= 0){
        	BiTerminalGraph socketArgs;
        	if(argNumber >= ARG_REGISTERS.size()){
            	// Take if off the stack and put it in the expected loc at the bottom
        		socketArgs = BiTerminalGraph.ofInstructions(
        			move(new Location(RSP, (argNumber + ARG_REGISTERS.size() + offset)*Architecture.BYTES_PER_ENTRY), Register.R10),
        			push(Register.R10));
        		offset++;
        	} else {
            	// Take if off the stack and put it in a register for the function call.
                socketArgs = BiTerminalGraph.ofInstructions(
            			move(new Location(RSP, (argNumber + ARG_REGISTERS.size() + offset)*Architecture.BYTES_PER_ENTRY),
            					ARG_REGISTERS.get(argNumber)));
        	}
        	
        	preCallEnd.setNext(socketArgs.getBeginning());
        	preCallEnd = socketArgs.getEnd();
        	
        	argNumber--;
        }
        
        
        BiTerminalGraph preCall = new BiTerminalGraph(preCallStart, preCallEnd);

        BiTerminalGraph call = BiTerminalGraph.ofInstructions(call(methodCall.getMethodName()));

        BiTerminalGraph postCall = BiTerminalGraph.sequenceOf(
                        // Remove the pushed arguments from the stack.
                        BiTerminalGraph.ofInstructions(add(new Literal(numOverflowingArgs * Architecture.WORD_SIZE), RSP)),
                        // Restore Arg Registers
                        RegisterSaver.popAll(),
                        // fix the scratch space
                        BiTerminalGraph.ofInstructions(add(new Literal(args.size() * Architecture.WORD_SIZE), RSP)),
                        // Put the return value on the stack
                        BiTerminalGraph.ofInstructions(push(RAX)));
        // TODO(jasonpr): Do 16-byte alignment.
        return BiTerminalGraph.sequenceOf(
        		preCall, 
        		call, 
        		postCall);
    }

    @Override
    public BiTerminalGraph getGraph() {
        return graph;
    }
}
