package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

public class ContinueStatement implements Statement {

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "continue";
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
}
