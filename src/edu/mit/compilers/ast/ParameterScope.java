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

}
