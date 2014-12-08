package edu.mit.compilers.codegen.controllinker;

import static edu.mit.compilers.codegen.asm.Register.R10;
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
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.subtract;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Location;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.optimization.ScopedVariable;

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

    private final MethodCall methodCall;
    private final Scope scope;
    private final Map<ScopedVariable, Register> allocations;

    public MethodCallGraphFactory(MethodCall methodCall, Scope scope, Map<ScopedVariable, Register> allocations) {
        this.methodCall = methodCall;
        this.scope = scope;
        this.allocations = allocations;
    }

    private FlowGraph<Instruction> calculateGraph(MethodCall methodCall, Scope scope) {
        BasicFlowGraph.Builder<Instruction> builder = BasicFlowGraph.builder();

        List<GeneralExpression> args = ImmutableList.copyOf(methodCall.getParameterValues());

        // Offset the stack.
        builder.append(subtract(new Literal(args.size() * Architecture.WORD_SIZE), RSP));

        // Stash args for call.  Evaluate them left to right, as Decaf specifies.
        for (int argNumber = 0; argNumber < args.size(); argNumber++) {
            builder.append(
                    // Put the arg on the stack.
                    new GeneralExprGraphFactory(args.get(argNumber), scope, allocations).getGraph())
                    // Stash the arg in a temp location.  We'll later move it into place
                    // as specified by the calling convention.
                    .append(pop(R10))
                    .append(move(R10,
                            new Location(RSP, argNumber * Architecture.BYTES_PER_ENTRY)));
        }

        builder.append(RegisterSaver.pushAllParameterRegisters());
        // Move stashed values to the spots specified by the x86 calling convention.
        int offset = 0;
        for (int argNumber = args.size() - 1; argNumber >= 0; argNumber--){
            Location argLocation = new Location(RSP,
                    (argNumber + ARG_REGISTERS.size() + offset) * Architecture.BYTES_PER_ENTRY);
        	if(argNumber >= ARG_REGISTERS.size()){
            	// Take if off the stack and put it in the expected loc at the bottom
                builder.append(move(argLocation, R10))
                        .append(push(R10));
                offset++;
        	} else {
            	// Take if off the stack and put it in a register for the function call.
                builder.append(move(argLocation, ARG_REGISTERS.get(argNumber)));
        	}
        }

        // TODO(jasonpr): Do 16-byte alignment.
        // Actually do the call.
        builder.append(call(methodCall.getMethodName()));

        int numOverflowingArgs = args.size() > ARG_REGISTERS.size()
                ? args.size() - ARG_REGISTERS.size()
                : 0;

        // Do post-call bookkeeping.
        // Remove the pushed arguments from the stack.
        builder.append(add(new Literal(numOverflowingArgs * Architecture.WORD_SIZE), RSP))
                .append(RegisterSaver.popAllParemeterRegsiters())
                // Remove scratch space.
                .append(add(new Literal(args.size() * Architecture.WORD_SIZE), RSP))
                // Push the return value to the stack.
                .append(push(RAX));

        return builder.build();
    }

    @Override
    public FlowGraph<Instruction> getGraph() {
        return calculateGraph(methodCall, scope);
    }
}
