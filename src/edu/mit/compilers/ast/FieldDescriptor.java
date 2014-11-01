package edu.mit.compilers.ast;

import com.google.common.base.Optional;

import edu.mit.compilers.common.Variable;


public class FieldDescriptor {

    final Variable variable;
    final Optional<IntLiteral> length;
    final BaseType type;
    final LocationDescriptor locationDescriptor;

    public FieldDescriptor(Variable variable, BaseType type, LocationDescriptor locationDescriptor) {
        this.variable = variable;
        this.length = Optional.<IntLiteral> absent();
        this.type = type;
        this.locationDescriptor = locationDescriptor;
    }

    public FieldDescriptor(Variable variable, IntLiteral length,
            BaseType type, LocationDescriptor locationDescriptor) {
        this.variable = variable;
        this.length = Optional.of(length);
        this.type = type;
        this.locationDescriptor = locationDescriptor;
    }

    public BaseType getType() {
        return type;
    }

    public Variable getVariable() {
        return variable;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((variable == null) ? 0 : variable.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (!(obj instanceof FieldDescriptor)) {
            return false;
        }
        FieldDescriptor other = (FieldDescriptor) obj;
        if (length == null) {
            if (other.length != null) {
                return false;
            }
        } else if (!length.equals(other.length)) {
            return false;
        }
        if (variable == null) {
            if (other.variable != null) {
                return false;
            }
        } else if (!variable.equals(other.variable)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }
}
