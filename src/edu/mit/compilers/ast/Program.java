package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;


public class Program implements Node {
    private final String programName;
    private final NodeSequence<Callout> callouts;
    private final Scope globals;
    private final NodeSequence<Method> methods;
    private final LocationDescriptor locationDescriptor;

    // TODO(jasonpr): bind program name
    public Program(List<Callout> callouts, Scope globals,
            List<Method> methods, LocationDescriptor locationDescriptor) {
        this.programName = "program";
        this.callouts = new NodeSequence<Callout>(callouts, "callouts");
        this.globals = globals;
        this.methods = new NodeSequence<Method>(methods, "methods");
        this.locationDescriptor = locationDescriptor;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of(callouts, methods);
    }

    @Override
    public String getName() {
        return this.programName;
    }

    public NodeSequence<Callout> getCallouts() {
        return callouts;
    }

    public Scope getGlobals() {
        return globals;
    }
    
    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public NodeSequence<Method> getMethods() {
        return methods;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
