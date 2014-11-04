package edu.mit.compilers.ast;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.semantics.Utils;

public class IntLiteral implements NativeLiteral {
    // Yes, a string.  We want the value that the user typed in.  We don't even care how Java
    // would represent it.
    private final String value;
    private final long longValue;
    private final LocationDescriptor locationDescriptor;

    public IntLiteral(String value, LocationDescriptor locationDescriptor) {
        this.value = value;
        // We are going to give the int literal the value 0 if its not valid
        // The semantic check should catch it
        this.longValue = Utils.isWithinBounds(value) ? Utils.parseLong(value) : 0L;
        this.locationDescriptor = locationDescriptor;
    }
    
    // Create a machine literal, pretty useful IMHO
    public IntLiteral(Long value) {
    	this.value = Long.toString(value);
    	this.longValue = value;
    	this.locationDescriptor = LocationDescriptor.machineCode();
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

    @Override
    public long get64BitValue() {
        return longValue;
    }

    public boolean equals(IntLiteral il) {
        return this.value.equals(il.getName())
                && this.get64BitValue() == il.get64BitValue()
                && this.getLocationDescriptor().equals(il.getLocationDescriptor());
    }

    // For equals and hashCode, we use the longValue, not the value.
    // Thus, "0x1" and "1" will be considered equal, as they should be.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
        if (!(obj instanceof IntLiteral)) {
            return false;
        }
        IntLiteral other = (IntLiteral) obj;
        if (longValue != other.longValue) {
            return false;
        }
        return true;
    }
}
