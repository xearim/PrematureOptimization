package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class TernaryOperation implements Statement {

    private final NativeExpression condition;
    private final NativeExpression trueResult;
    private final NativeExpression falseResult;

    public TernaryOperation(NativeExpression condition, NativeExpression trueResult,
            NativeExpression falseResult) {
        this.condition = condition;
        this.trueResult = trueResult;
        this.falseResult = falseResult;
    }

    @Override
    public Iterable<? extends NativeExpression> getChildren() {
        return ImmutableList.of(condition, trueResult, falseResult);
    }

    @Override
    public String getName() {
        return "ternary";
    }

    @Override
    public boolean canReturn() {
        return false;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return ImmutableList.of();
    }
}
