package edu.mit.compilers.ast;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class TernaryOperation implements NativeExpression {
    private final NativeExpression condition;
    private final NativeExpression trueResult;
    private final NativeExpression falseResult;
    private final LocationDescriptor locationDescriptor;
    private final ExpressionType type = ExpressionType.TERNARY_OPERATION;

    public TernaryOperation(NativeExpression condition, NativeExpression trueResult,
            NativeExpression falseResult, LocationDescriptor locationDescriptor) {
        this.condition = condition;
        this.trueResult = trueResult;
        this.falseResult = falseResult;
        this.locationDescriptor = locationDescriptor;
    }
    
    public TernaryOperation(NativeExpression condition, NativeExpression trueResult,
            NativeExpression falseResult) {
    	this(condition, trueResult, falseResult, LocationDescriptor.machineCode());
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
    
    public String asText() {
    	return condition.asText() + " " + "?" + " " + trueResult.asText() + " " + ":" + " " + falseResult.asText();
    }
    
    public ExpressionType getType(){
    	return type;
    }
    
    public int compareTo(NativeExpression other){
    	Preconditions.checkState(other != null);
    	return this.getType().getPrecedence() - other.getType().getPrecedence();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result
                + ((falseResult == null) ? 0 : falseResult.hashCode());
        result = prime * result
                + ((trueResult == null) ? 0 : trueResult.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TernaryOperation)) {
            return false;
        }
        TernaryOperation other = (TernaryOperation) obj;
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (falseResult == null) {
            if (other.falseResult != null) {
                return false;
            }
        } else if (!falseResult.equals(other.falseResult)) {
            return false;
        }
        if (trueResult == null) {
            if (other.trueResult != null) {
                return false;
            }
        } else if (!trueResult.equals(other.trueResult)) {
            return false;
        }
        return true;
    }

    @Override
    public NativeExpression withReplacements(NativeExpression toReplace,
            NativeExpression replacement) {
        if (this.equals(toReplace)) {
            return replacement;
        }
        return new TernaryOperation(
                condition.withReplacements(toReplace, replacement),
                trueResult.withReplacements(toReplace, replacement),
                falseResult.withReplacements(toReplace, replacement),
                locationDescriptor);
    }
    
    public TernaryOperation replaceFirst(NativeExpression toReplace,
            NativeExpression replacement) {
        if (condition.equals(toReplace)) {
        	return new TernaryOperation(
                    replacement,
                    trueResult,
                    falseResult,
                    locationDescriptor);
        } else if (trueResult.equals(toReplace)){
        	return new TernaryOperation(
                    condition,
                    replacement,
                    falseResult,
                    locationDescriptor);
        } else if (falseResult.equals(toReplace)){
        	return new TernaryOperation(
                    condition,
                    trueResult,
                    replacement,
                    locationDescriptor);
        } else {
        	return this;
        }
    }
}
