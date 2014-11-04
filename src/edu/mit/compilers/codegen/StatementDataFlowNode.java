package edu.mit.compilers.codegen;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;

/** A data flow node that represents an individual statement. */
public abstract class StatementDataFlowNode extends SequentialDataFlowNode {
    
    // Forward the constructors from StatementDataFlowNode.
    protected StatementDataFlowNode(Optional<DataFlowNode> prev,
            Optional<DataFlowNode> next, String name) {
        super(prev, next, name);
    }

    protected StatementDataFlowNode(String name) {
        super(name);
    }

    public abstract Scope getScope();
    public abstract Optional<? extends NativeExpression> getExpression();
}
