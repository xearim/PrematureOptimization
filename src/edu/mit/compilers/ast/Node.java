package edu.mit.compilers.ast;

public interface Node {
	
    /** 
     * Get this node's children, in some meaningful order.
     * 
     * <p>This will be used by code that walks an AST, such as an AST-printer.
     */
    public Iterable<? extends Node> getChildren();

    /** Get a name that describes this node, but not necessarily its children. */
    public String getName();
}
