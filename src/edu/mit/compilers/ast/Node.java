package edu.mit.compilers.ast;

import com.google.common.base.Optional;

public interface Node {
	
	
	/**
	 * Returns the BaseType that this node returns when evaluated
	 * as an expression
	 */
	public Optional<BaseType> evalType();

	/**
	 * Return a boolean expressing whether or not the node
	 * can return a value of type BaseType, an absent optional represents
	 * the void return type
	 */
	public boolean canReturn(Optional<BaseType> type);
	
	/**
	 * Return a boolean expressing if the node can only
	 * return a value of type BaseType, an absent optional represents
	 * the void return type
	 */
	public boolean mustReturn(Optional<BaseType> type);
	
    /** 
     * Get this node's children, in some meaningful order.
     * 
     * <p>This will be used by code that walks an AST, such as an AST-printer.
     */
    public Iterable<? extends Node> getChildren();

    /** Get a name that describes this node, but not necessarily its children. */
    public String getName();
}
