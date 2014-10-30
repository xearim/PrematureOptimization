package edu.mit.compilers.ast;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

public class StringLiteral implements GeneralExpression {
	private static long stringIDGenerator = 0;
	private static Collection<StringLiteral> stringLiteralSet = new ArrayList<StringLiteral>();

    private final String value;
    private final LocationDescriptor locationDescriptor;
    private final long stringID;

    public StringLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
        this.locationDescriptor = locationDescriptor;
        this.stringID = stringIDGenerator++;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
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
    
    public static void addString(StringLiteral sl) {
    	stringLiteralSet.add(sl);
    }

    // TODO(jasonpr): Consider excluding value, since stringID is already unique.
    // Alternatively, consider doing some interning scheme, so that duplicate strings
    // need not be duplicated in the generated code.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (stringID ^ (stringID >>> 32));
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StringLiteral)) {
            return false;
        }
        StringLiteral other = (StringLiteral) obj;
        if (stringID != other.stringID) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
