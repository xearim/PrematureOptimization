package edu.mit.compilers.ast;

/** A callout_arg as defined in the Decaf spec, i.e., the union of expr and string_literal. */
public interface GeneralExpression extends Node {

    // TODO(jasonpr): Remove this method from GeneralExpression once it's added
    // to Node.
    public LocationDescriptor getLocationDescriptor();
    
    @Override
    public Iterable<? extends GeneralExpression >getChildren();
    
    public String asText();
}
