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
        this.longValue = value.charAt(0);
        this.locationDescriptor = locationDescriptor;
    }

    /*
     * TODO(Manny): Initial testing suggest that we don't have to do anything
     * different for irregular characters. This should be fully verified.
     */
    private boolean isChar(String value) {
        return value.length() == 1;
    }

    @Override
    public Iterable<? extends Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "'" + value + "'";
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    @Override
    public long get64BitValue() {
        return this.longValue;
    }

    public boolean equals(CharLiteral cl) {
        return this.getName().equals(cl.getName())
                && this.get64BitValue() == cl.get64BitValue()
                && this.getLocationDescriptor().equals(cl.getLocationDescriptor());
    }

    // TODO(jasonpr): Implement hashCode, and toString.
    // TODO(jasonpr): Implement class-specific accessors.
}
