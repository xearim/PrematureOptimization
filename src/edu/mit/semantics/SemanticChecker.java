package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import edu.mit.compilers.ast.Callout;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.Node;
import edu.mit.compilers.ast.NodeSequence;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.Scope;
import edu.mit.semantics.errors.DeclaredTwiceScopeSemanticError;
import edu.mit.semantics.errors.LocationInFile;
import edu.mit.semantics.errors.SemanticError;

public class SemanticChecker {
	private Program prog;
	private List<SemanticError> errors = new ArrayList<SemanticError>();
	
	public SemanticChecker(Program ir) {
		this.prog = ir;
	}
	
	public List<SemanticError> checkProgram() {
		/*
		 * Implement checks
		 */
		checkCallouts(this.prog.getCallouts());
		checkGlobals(this.prog.getGlobals());
//		checkMethods(this.prog.getMethods());
		
		return errors;
	}
	
	/**
	 * Callouts check
	 * 
	 * Only need to check for duplicates.
	 */
	private void checkCallouts(NodeSequence<Callout> callouts) {
		List<String> uniqueFields = new ArrayList<String>();
		List<String> duplicateFields = new ArrayList<String>();
		
		// Get duplicates
		/* TODO: Manny: Clarify the correct way to do this */
		@SuppressWarnings("unchecked")
		Iterable<? extends Node> calloutItr =  callouts.getChildren();
		for (Node callout: calloutItr) {
//		for (Node callout = calloutItr.next(); calloutItr.hasNext();) {
			String calloutName = callout.getName();
			
			if (uniqueFields.contains(calloutName)) {
				if (!duplicateFields.contains(calloutName)) {
					duplicateFields.add(calloutName);
				}
			} else {
				uniqueFields.add(calloutName);
			}
		}
		
		for (String calloutName: duplicateFields) {
//			errors.add(/* CREATE SEMANTIC ERROR OBJECT */);
		}
	}
	
	/**
	 * Global scope check.
	 * 
	 * Only need to check for duplicates
	 */
	private void checkGlobals(Scope globals) {
		checkUniqueFieldNames(globals);
	}
	
	/*
	 * Abandoned for traveler pattern
	 */
//	/**
//	 * Checks if a field has been declared twice in all scopes
//	 */
//	private void declaredTwice() {
//		// Check global scope
//		checkUniqueFieldNames(this.prog.getGlobals());
//		
//		// Check each method
//		/* TODO: Manny: Clarify the correct way to do this */
//		NodeSequence<Method> methods = this.prog.getMethods();
//		@SuppressWarnings("unchecked")
//		UnmodifiableIterator<Method> methodItr = (UnmodifiableIterator<Method>) methods.getChildren().iterator();
//		
//		for (Method method = methodItr.next(); methodItr.hasNext();) {
//			// check locals and params
//			checkUniqueFieldNames(method.getName(), false, method.getLocals());
//			checkUniqueFieldNames(method.getName(), true, method.getParameters());
//			
//			// check sub-local
//			/* TODO: Manny: recurse through statements in block node */
////			UnmodifiableIterator<? extends Node> statements = method.getChildren();
//		}
//	}
	
	
	
	/**
	 * Checks unique field names in a scope. Intended for global scope.
	 */
	private void checkUniqueFieldNames(Scope scope){
		checkUniqueFieldNames(null,null,scope);
	}
	
	/**
	 * Checks unique field names in a scope. Intended for param and local scope.
	 */
	private void checkUniqueFieldNames(
			String methodName, Boolean isParam,
			Scope scope) {
		ImmutableList<FieldDescriptor> fields = scope.getVariables();
		HashMap<String, List<LocationInFile>> nameToLocations = new HashMap<String, List<LocationInFile>>();

		// Record which line numbers each field goes to
		for (FieldDescriptor field : fields) {
			String fieldName = field.getName();
			
			if (nameToLocations.containsKey(fieldName)) {
				nameToLocations.get(fieldName).add(
						new LocationInFile( field.getLineNumber(), field.getColumnNumber()));
			} else {
				nameToLocations.put(fieldName, new ArrayList<LocationInFile>());
				nameToLocations.get(fieldName).add(
						new LocationInFile( field.getLineNumber(), field.getColumnNumber()));
			}
		}
		
		// Any fields that have multiple line numbers were declared multiple times
		for (String fieldName : nameToLocations.keySet()) {
			List<LocationInFile> locs = nameToLocations.get(fieldName); 
			if (locs.size() > 1) {
				this.errors.add(new DeclaredTwiceScopeSemanticError(fieldName,methodName,isParam,scope,locs));
			}
		}
	}
}
