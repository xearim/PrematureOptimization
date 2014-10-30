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

    /** Calculates hashCode, ignoring programName, in addition to locationDescriptor! */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((callouts == null) ? 0 : callouts.hashCode());
        result = prime * result + ((globals == null) ? 0 : globals.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        return result;
    }

    /** Computes equality, ignoring programName, in addition to locationDescriptor! */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Program)) {
            return false;
        }
        Program other = (Program) obj;
        if (callouts == null) {
            if (other.callouts != null) {
                return false;
            }
        } else if (!callouts.equals(other.callouts)) {
            return false;
        }
        if (globals == null) {
            if (other.globals != null) {
                return false;
            }
        } else if (!globals.equals(other.globals)) {
            return false;
        }
        if (methods == null) {
            if (other.methods != null) {
                return false;
            }
        } else if (!methods.equals(other.methods)) {
            return false;
        }
        return true;
    }
}
