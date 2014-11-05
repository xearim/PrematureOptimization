package edu.mit.compilers.ast;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;

public class CharLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final long longValue;
    private final LocationDescriptor locationDescriptor;

    public CharLiteral(String value, LocationDescriptor locationDescriptor) {
        checkArgument(isChar(value));

        this.value = value;
        // casting converts between longs and chars
        this.longValue = value.charAt(1);
        this.locationDescriptor = locationDescriptor;
    }

    /*
     * TODO(Manny): Initial testing suggest that we don't have to do anything
     * different for irregular characters. This should be fully verified.
     * 
     * A Char is a string of "'" + "char" + "'"
     */
    private boolean isChar(String value) {
        return value.length() == 3;
    }

    @Override
    public Iterable<? extends GeneralExpression> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return value;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public long get64BitValue() {
        return this.longValue;
    }
    
    public String asText() {
    	return "'" + value + "'";
    }

    public boolean equals(CharLiteral cl) {
        return this.getName().equals(cl.getName())
                && this.get64BitValue() == cl.get64BitValue()
                && this.getLocationDescriptor().equals(cl.getLocationDescriptor());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
        if (!(obj instanceof CharLiteral)) {
            return false;
        }
        CharLiteral other = (CharLiteral) obj;
        if (longValue != other.longValue) {
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
