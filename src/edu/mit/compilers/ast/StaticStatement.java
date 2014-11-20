package edu.mit.compilers.ast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * A statement that does not specify any connections in a flow graph.
 *
 * <p>This could have been described as a statement that does not alter
 * the path of execution, but that might be misleading: a method call is
 * a static statement!
 */
public abstract class StaticStatement implements Statement {

    /** Return the single expression contained in this statement, if there is one. */
    protected abstract Optional<NativeExpression> expression();

    public final boolean hasExpression() {
        return expression().isPresent();
    }

    public final NativeExpression getExpression() {
        return expression().get();
    }

    @Override
    public final Iterable<Block> getBlocks() {
        return ImmutableList.of();
    }
}
