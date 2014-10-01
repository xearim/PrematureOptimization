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


    // The program root doesn't have a meaningful return value
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return false;
	}

	// The program root doesn't have a meaningful return value
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		return false;
	}

	// Nor a meaningful evaluation type
	@Override
	public Optional<BaseType> evalType() {
		return Optional.absent();
	}
    
    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
