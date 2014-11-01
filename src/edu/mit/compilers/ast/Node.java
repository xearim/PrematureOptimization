package edu.mit.compilers.ast;

/**
 * An node of an AST.
 *
 * <p>In addition to storing it properties and its children, a Node implementation
 * is expect to contain a LocationDescriptor, which identifies the line and column number
 * at which the node begins.  (This is not yet built into the Node interface, because we
 * have not figured out how to meaningfully git a LocationDescriptor to NodeSequence, yet.)
 * But, the #equals and #hashCode methods of an implementation MUST NOT use the
 * LocationDescriptor.  That is, the Nodes for for two identical AST nodes that are generated
 * at different lines in some Decaf source must compare equal.
 */
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
