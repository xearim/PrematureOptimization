package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.Register.R11;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class CompareGraphFactory implements GraphFactory {

    private final NativeExpression leftArg;
    private final NativeExpression rightArg;
    private final Scope scope;
    
    public CompareGraphFactory(NativeExpression leftArg, NativeExpression rightArg, Scope scope){
    	this.leftArg = leftArg;
    	this.rightArg = rightArg;
        this.scope = scope;
    }
    
    @Override
    public BiTerminalGraph getGraph() {
            return BiTerminalGraph.sequenceOf(
                    new NativeExprGraphFactory(leftArg, scope).getGraph(),
                    new NativeExprGraphFactory(rightArg, scope).getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(R11),
                            pop(R10),
                            compareFlagged(R10, R11)
                            )
                    );
    }

}

