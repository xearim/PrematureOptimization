package edu.mit.compilers.ast;

/** An Node of an AST that is a Decaf statement. */
public interface Statement extends Node {
	
	/**
	 * Returns the Blocks at a recursive depth of 1 below the current statement
	 * This gives back the block(s) underneath an if, while, and for loop
	 * while returning an empty iterable for all other statement types
	 * 
	 * Essentially allows one to recurse through the depth of blocks using only this
	 * method and a Block's getStatements method.
	 */
	public Iterable<Block> getBlocks();
}
