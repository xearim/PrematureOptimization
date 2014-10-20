package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph.ControlNodes;
import edu.mit.compilers.codegen.controllinker.statements.StatementGraphFactory;

public class BlockGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public BlockGraphFactory(Block block) {
        this.graph = calculateGraph(block);
    }

    private ControlTerminalGraph calculateGraph(Block block) {
        SequentialControlFlowNode start = SequentialControlFlowNode.namedNop("Block start");
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("Block end");
        SequentialControlFlowNode continueNode = SequentialControlFlowNode.namedNop("Block continue");
        SequentialControlFlowNode breakNode = SequentialControlFlowNode.namedNop("Block break");
        SequentialControlFlowNode returnNode = SequentialControlFlowNode.namedNop("Block return");

        SequentialControlFlowNode currentNode = start;
        // Zero out variables
        for(FieldDescriptor variable: block.getScope().getVariables()){
            SequentialControlFlowNode nextNode = zeroOutVariable(variable, block.getScope(), currentNode);
            currentNode = nextNode;
        }
        
        for (Statement statement : block.getStatements()) {
            ControlTerminalGraph statementGraph = 
                    new StatementGraphFactory(statement, block.getScope()).getGraph();

            // Hook up control flow nodes
            statementGraph.getControlNodes().getContinueNode().setNext(continueNode);
            statementGraph.getControlNodes().getBreakNode().setNext(breakNode);
            statementGraph.getControlNodes().getReturnNode().setNext(returnNode);

            // Hook up statement graph to previous
            currentNode.setNext(statementGraph.getBeginning());
            currentNode = statementGraph.getEnd();
        }

        currentNode.setNext(end);

        return new ControlTerminalGraph(start, end,
                    new ControlNodes(breakNode, continueNode, returnNode));
    }
    
    /**
     * Initialize a given variable in a given scope
     */
    private SequentialControlFlowNode zeroOutVariable(FieldDescriptor variable, Scope scope, SequentialControlFlowNode start){
        if(variable.getLength().isPresent()){
            return zeroOutArray(variable, scope, start);
        } else {
            return zeroOutScalar(variable, scope, start);
        }
    }
    
    /**
     * Assign an array variable to the default initial value of a variable for each element of the array
     * and patch that in as the ControlFlowNode following the start
     * 
     * returns the newly created node containing the zero-ing instructions following the start 
     */
    private SequentialControlFlowNode zeroOutArray(FieldDescriptor variable, Scope scope, SequentialControlFlowNode start){
        SequentialControlFlowNode hook = start;
        for(int i = 0; i < variable.getLength().get().get64BitValue(); i++){
            // TODO(jasonpr): Zero it out.
        }
        return hook;
    }
    
    /**
     * Assign a scalar variable to the default initial value of a variable and patch that in as the 
     * ControlFlowNode following the start
     * 
     * returns the newly created node containing the zero-ing instructions following the start 
     */
    private SequentialControlFlowNode zeroOutScalar(FieldDescriptor variable, Scope scope, SequentialControlFlowNode start){
        SequentialControlFlowNode hook = start;
        SequentialControlFlowNode next = SequentialControlFlowNode.terminal(Instructions.move(Literal.INITIAL_VALUE, 
                                                new VariableReference(variable.getName(), scope)));
        hook.setNext(next);
        hook = next;
        return hook;
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }
}
