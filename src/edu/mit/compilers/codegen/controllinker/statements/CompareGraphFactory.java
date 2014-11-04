package edu.mit.compilers.codegen.controllinker.statements;

import static edu.mit.compilers.codegen.asm.Register.R10;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.compareFlagged;
import static edu.mit.compilers.codegen.asm.instructions.Instructions.pop;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.controllinker.BiTerminalGraph;
import edu.mit.compilers.codegen.controllinker.GraphFactory;
import edu.mit.compilers.codegen.controllinker.NativeExprGraphFactory;

public class CompareGraphFactory implements GraphFactory {

    private final NativeExpression comparison;
    private final Scope scope;
    
    public CompareGraphFactory(NativeExpression comparison, Scope scope){
        this.comparison = comparison;
        this.scope = scope;
    }
    
    @Override
    public BiTerminalGraph getGraph() {
            return BiTerminalGraph.sequenceOf(
                    new NativeExprGraphFactory(comparison, scope).getGraph(),
                    BiTerminalGraph.ofInstructions(
                            pop(R10),
                            compareFlagged(R10, Literal.TRUE)
                            )
                    );
    }

}

