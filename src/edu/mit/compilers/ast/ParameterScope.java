package edu.mit.compilers.ast;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class ParameterScope extends Scope{

    private ImmutableList<BaseType> signature;
    private final LocationDescriptor locationDescriptor;

    public ParameterScope(List<FieldDescriptor> variables, Scope parent,
            LocationDescriptor locationDescriptor) {
        super(variables, parent);
		List<BaseType> signatureSet = new ArrayList<BaseType>();
		for(FieldDescriptor var: variables)
			signatureSet.add(var.type);
		signature = ImmutableList.copyOf(signatureSet);
		this.locationDescriptor = locationDescriptor;
	}

	public ImmutableList<BaseType> getSignature(){
		return signature;
	}

    public LocationDescriptor getLocationDescriptor() {
	return locationDescriptor;
    }

    @Override
    public ScopeType getScopeType() {
        return ScopeType.PARAMETER;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((signature == null) ? 0 : signature.hashCode());
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
        if (!(obj instanceof ParameterScope)) {
            return false;
        }
        ParameterScope other = (ParameterScope) obj;
        if (signature == null) {
            if (other.signature != null) {
                return false;
            }
        } else if (!signature.equals(other.signature)) {
            return false;
        }
        return true;
    }
}
