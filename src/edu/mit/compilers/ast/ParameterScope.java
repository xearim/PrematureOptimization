package edu.mit.compilers.ast;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class ParameterScope extends Scope{

	private ImmutableList<BaseType> signature;
	
	public ParameterScope(List<FieldDescriptor> variables) {
		super(variables);
		List<BaseType> signatureSet = new ArrayList<BaseType>();
		for(FieldDescriptor var: variables)
			signatureSet.add(var.type);
		signature = ImmutableList.copyOf(signatureSet);
	}
	
	public ImmutableList<BaseType> getSignature(){
		return signature;
	}

}
