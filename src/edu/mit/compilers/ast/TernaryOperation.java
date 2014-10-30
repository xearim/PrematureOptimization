package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class TernaryOperation implements NativeExpression {
    private final NativeExpression condition;
    private final NativeExpression trueResult;
    private final NativeExpression falseResult;
    private final LocationDescriptor locationDescriptor;

    public TernaryOperation(NativeExpression condition, NativeExpression trueResult,
            NativeExpression falseResult, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.trueResult = trueResult;
        this.falseResult = falseResult;
        this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends NativeExpression> getChildren() {
        return ImmutableList.of(condition, trueResult, falseResult);
    }

    @Override
    public String getName() {
        return "ternary";
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NativeExpression getCondition() {
        return condition;
    }

    public NativeExpression getTrueResult() {
        return trueResult;
    }

    public NativeExpression getFalseResult() {
        return falseResult;
    }
}
