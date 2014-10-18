package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.ast.ArrayLocation;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;
import edu.mit.compilers.codegen.controllinker.VariableLoadGraphFactory;
import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.add;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.subtract;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.move;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.moveToArray;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.push;

public class AssignmentGraphFactory implements GraphFactory {

	private final NativeExpression expr;
	private final AssignmentOperation op;
	private final Scope scope;
	private final Location target;
	
	public AssignmentGraphFactory(Location target, AssignmentOperation op, NativeExpression expr, Scope scope){
		this.target = target;
		this.op = op;
		this.expr = expr;
		this.scope = scope;
	}
	
	private BiTerminalGraph calculateStore(Location target, Scope scope){
		if (target instanceof ArrayLocation) {
            return calculateStoreToArray((ArrayLocation) target, scope);
        } else if (target instanceof ScalarLocation) {
            return calculateStoreToScalar((ScalarLocation) target, scope);
        } else {
            throw new AssertionError("Unexpected location type for " + target);
        }
	}
	
	private BiTerminalGraph calculateStoreToArray(ArrayLocation target, Scope scope){
		String name = target.getVariableName();
        NativeExpression index = target.getIndex();
        long bytesPerEntry = 8;

        return BiTerminalGraph.sequenceOf(
                new NativeExprGraphFactory(index, scope).getGraph(),
                BiTerminalGraph.ofInstructions(
                        pop(R11),
                        pop(R10),
                        moveToArray(R10, R11, bytesPerEntry, 
                        		new VariableReference(name, scope))
                        ));
	}
	
	private BiTerminalGraph calculateStoreToScalar(ScalarLocation target, Scope scope){
		String name = target.getName();
        return BiTerminalGraph.ofInstructions(
        		pop(R11),
                move(R11, new VariableReference(name, scope))
                );
	}
	
	@Override
	public BiTerminalGraph getGraph() {
		switch(op){
		case MINUS_EQUALS:
			return BiTerminalGraph.sequenceOf(
					new VariableLoadGraphFactory(target, scope).getGraph(),
					new NativeExprGraphFactory(expr, scope).getGraph(),
					BiTerminalGraph.ofInstructions(
							pop(R10),
							pop(R11),
							subtract(R11, R10),
							push(R10)
							),
					calculateStore(target, scope)
					);
		case PLUS_EQUALS:
			return BiTerminalGraph.sequenceOf(
					new VariableLoadGraphFactory(target, scope).getGraph(),
					new NativeExprGraphFactory(expr, scope).getGraph(),
					BiTerminalGraph.ofInstructions(
							pop(R10),
							pop(R11),
							add(R11, R10),
							push(R10)
							),
					calculateStore(target, scope)
					);
		case SET_EQUALS:
			return BiTerminalGraph.sequenceOf(
						new NativeExprGraphFactory(expr, scope).getGraph(),
						calculateStore(target, scope)
						);
		default:
			throw new AssertionError("Unexpected operator: " + op.getSymbol());
		
		}
	}

}
