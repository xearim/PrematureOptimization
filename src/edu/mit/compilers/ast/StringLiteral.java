package edu.mit.compilers.ast;

import java.util.Set;

import com.google.common.collect.ImmutableList;

public class StringLiteral implements GeneralExpression {
	private static long stringIDGenerator;
	private static Set<StringLiteral> stringLiteralSet;

    private final String value;
    private final LocationDescriptor locationDescriptor;
    private final long stringID;

    public StringLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
        this.locationDescriptor = locationDescriptor;
        this.stringID = stringIDGenerator++;
        stringLiteralSet.add(this);
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "\"" + value + "\"";
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }
    
    public String getID() {
    	return Long.toString(stringID);
    }
    
    public static Iterable<StringLiteral> getStringLiterals() {
    	return stringLiteralSet;
    }

    // TODO(jasonpr): Implement equals, hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
