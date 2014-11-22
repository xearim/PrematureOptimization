package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Examines the truthiness of a condition.
 *
 * <p>This "statement" is not a true Decaf statement.  Rather, it represents
 * the examination of a condition, in order to decide where control should
 * flow.
 * 
 * <p>On an x86 architecture, this statement sets the flag bits so that, for
 * example, a JNE instruction can be executed meaningfully.
 */
public class Condition extends StaticStatement {

    private final NativeExpression expr;

    public Condition(NativeExpression expr) {
        this.expr = expr;
    }

    @Override
    protected Optional<NativeExpression> expression() {
        return Optional.of(expr);
    }

    @Override
    public Iterable<NativeExpression> getChildren() {
        return ImmutableList.of(expr);
    }

    @Override
    public String getName() {
        return "Condition";
    }

    @Override
    public boolean canReturn() {
        return false;
    }

    @Override
    public long getMemorySize() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
        if (!(obj instanceof Condition)) {
            return false;
        }
        Condition other = (Condition) obj;
        if (expr == null) {
            if (other.expr != null) {
                return false;
            }
        } else if (!expr.equals(other.expr)) {
            return false;
        }
        return true;
    }
}
