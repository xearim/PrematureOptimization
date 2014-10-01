package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class Program implements Node {
    private final NodeSequence<Callout> callouts;
    private final Scope globals;
    private final NodeSequence<Method> methods;

    public Program(List<Callout> callouts, List<FieldDescriptor> globals,
            List<Method> methods) {
        this.callouts = new NodeSequence<Callout>(callouts, "callouts");
        this.globals = new Scope(globals);
        this.methods = new NodeSequence<Method>(methods, "methods");
    }
    
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(callouts, methods);
    }

    @Override
    public String getName() {
        return "program";
    }
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
