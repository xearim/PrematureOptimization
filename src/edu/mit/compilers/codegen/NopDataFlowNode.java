package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.GeneralExpression;

public class NopDataFlowNode extends SequentialDataFlowNode {
    private NopDataFlowNode(String name) {
        super(name);
    }
    
    public static NopDataFlowNode nop() {
        return new NopDataFlowNode("");
    }
    
    public static NopDataFlowNode nopNamed(String name) {
        return new NopDataFlowNode(name);
    }

    @Override
    public Collection<GeneralExpression> getExpressions() {
        return ImmutableList.of();
    }
}
