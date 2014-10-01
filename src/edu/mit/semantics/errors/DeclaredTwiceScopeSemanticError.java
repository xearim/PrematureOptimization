package edu.mit.semantics.errors;

import java.util.List;

import edu.mit.compilers.ast.Scope;

/**
 * This class represents an error that violates the first rule:
 * 1) No identifier is declared twice in the same scope.
 * This includes 'callout' identifiers, which exist in the global scope.
 */
public class DeclaredTwiceScopeSemanticError implements SemanticError {
	private Boolean isParam; // null means that it is global scope
	private Scope scope;
	private String fieldName, methodName;
	private List<LocationInFile> locations;
	
	/*
	 * null isParam represents global. In that case, methodName isn't
	 * necessary and can be left as null.
	 */
	public DeclaredTwiceScopeSemanticError(String fieldName, String methodName, Boolean isParam, Scope scope, List<LocationInFile> locs) {
		this.fieldName = fieldName;
		this.methodName = methodName;
		this.isParam = isParam;
		this.scope = scope;
	}

	@Override
	public String generateErrorMessage() {
		String returnString = String.format("%svariable name \"%s\" used multiple times in %s scope%s.",
				getLocationsString(), this.fieldName, getScopeString(), getMethodNameString()); 
		return returnString;
	}
	
	/**
	 * If locations recorded are (3,15) and (5,2),
	 * returns "In 3:15, 5:2. "
	 * 
	 * (A,B), A is the line number, B is the column
	 */
	private String getLocationsString() {
		String locationsString = "In ";
		for (LocationInFile loc : locations) {
			locationsString += String.format("%d:%d, ", loc.getLineNumber(), loc.getColumnNumber());
		}
		return locationsString;
	}
	
	/**
	 * Returns appropriate scope type.
	 */
	private String getScopeString() {
		if (this.isParam == null) {
			return "global";
		} else if (this.isParam) {
			return "parameter";
		} else {
			return "local";
		}
	}
	
	/**
	 * If global, then no need for this string, otherwise returns
	 * " in method foo" for fields in the method "foo".
	 */
	private String getMethodNameString() {
		if (this.isParam == null) {
			return "";
		}
		return String.format(" in method %s", this.methodName);
	}
}
