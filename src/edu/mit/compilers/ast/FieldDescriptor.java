package edu.mit.compilers.ast;

import com.google.common.base.Optional;


public class FieldDescriptor {

    final String name;
    final Optional<IntLiteral> length;
    final BaseType type;
    final LocationDescriptor locationDescriptor;

    public FieldDescriptor(String name, BaseType type, LocationDescriptor locationDescriptor) {
        this.name = name;
        this.length = Optional.<IntLiteral> absent();
        this.type = type;
        this.locationDescriptor = locationDescriptor;
    }

    public FieldDescriptor(String name, IntLiteral length,
            BaseType type, LocationDescriptor locationDescriptor) {
        this.name = name;
        this.length = Optional.of(length);
        this.type = type;
        this.locationDescriptor = locationDescriptor;
    }

    public BaseType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Optional<IntLiteral> getLength() {
        return length;
    }

    public LocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    public int getLineNumber() {
        return locationDescriptor.lineNo();
    }

    public int getColumnNumber() {
        return locationDescriptor.colNo();
    }

    /** Get the size of this field, as a number of 64-bit quadwords. */
    public long getSize() {
        if (length.isPresent()) {
            // It's an array.
            return length.get().get64BitValue();
        } else {
            // It's a scalar.
            return 1;
        }
    }
}
