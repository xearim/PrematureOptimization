package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.ast.Callout;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.Node;
import edu.mit.compilers.ast.NodeSequence;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.semantics.errors.DeclaredTwiceSemanticError;
import edu.mit.semantics.errors.LocationType;
import edu.mit.semantics.errors.SemanticError;

/**
 * Checks for the following rule/s:
 *
 * 1) No identifier is declared twice in the same scope.
 * This includes 'callout' identifiers, which exist in the global scope.
 */
public class DeclaredTwiceSemanticCheck implements SemanticCheck {
	private List<SemanticError> errors = new ArrayList<SemanticError>();
	Program prog;
	
	public DeclaredTwiceSemanticCheck(Program prog) {
		this.prog = prog;
	}
	
	public List<SemanticError> doCheck() {
		checkCallouts(prog.getCallouts());
		checkGlobals(prog.getGlobals());
		checkMethods(prog.getMethods());
		
		return this.errors;
	}
	
	/**
	 * Callouts check
	 * 
	 * Only need to check for duplicates.
	 * TODO(Manny): Make it take into account location
	 */
	private void checkCallouts(NodeSequence<Callout> callouts) {
		List<String> uniqueFields = new ArrayList<String>();
		List<String> duplicateFields = new ArrayList<String>();
		
		// Get duplicates
		Iterable<? extends Node> calloutItr =  callouts.getChildren();
		for (Node callout: calloutItr) {
			String calloutName = callout.getName();
			
			if (uniqueFields.contains(calloutName)) {
				if (!duplicateFields.contains(calloutName)) {
					duplicateFields.add(calloutName);
				}
			} else {
				uniqueFields.add(calloutName);
			}
		}

		// Generate errors for each duplicate
		for (String calloutName: duplicateFields) {
			/*
			 * TODO(Manny) add locations
			 */
			this.errors.add(new DeclaredTwiceSemanticError(LocationType.CALLOUT,
					null, this.prog.getName(), calloutName, null));
		}
	}
	
	/**
	 * Globals check 
	 *
	 * Checks unique field names in global scope
	 */
	private void checkGlobals(Scope scope){
		checkScope(LocationType.GLOBAL, this.prog.getName(),scope,null);
	}
	
	/**
	 * Methods check
	 *
	 * Checks each scope in each method
	 */
	private void checkMethods(NodeSequence<Method> methods) {
		/*
		 * The cast is necessary, because we need to access elements that are
		 * in specifically in Method. The Program spec guarantees that this is
		 * a valid cast.
		 */
		@SuppressWarnings("unchecked")
		Iterable<Method> methodItr = (Iterable<Method>) methods.getChildren();

		// Iterate through each method
		for (Method method : methodItr) {
			// Param scope
			checkScope(LocationType.PARAM, this.prog.getName(), 
					method.getParameters(), method.getName());
			// Recurse on Local scope
			checkBlock(method.getBlock(), method.getName());
			
		}
	}
	
	private void checkBlock(Block block, String methodName) {
		// Check the immediate scope
		checkScope(LocationType.LOCAL, this.prog.getName(),
				block.getScope(), methodName);
		
		// Check potential scopes within statements
		Iterable<Statement> statements = block.getStatements();
		
		for (Statement statement : statements) {
			Iterable<Block> subblocks = statement.getBlocks();
			
			for (Block subblock : subblocks) {
				checkBlock(subblock, methodName);
			}
		}
	}
	
	/**
	 * Checks unique field names in a scope. General enough for all scopes,
	 * If intended for global, make methodName is null.
	 */
	private void checkScope(LocationType type, String programName, Scope scope,
			String methodName){
		ImmutableList<FieldDescriptor> fields = scope.getVariables();
		HashMap<String, List<LocationDescriptor>> nameToLocations = new HashMap<String, List<LocationDescriptor>>();

		/*
		 * Get duplicates and record line numbers each field goes to
		 */
		for (FieldDescriptor field : fields) {
			String fieldName = field.getName();
			
			if (!nameToLocations.containsKey(fieldName)) {
				nameToLocations.put(fieldName, new ArrayList<LocationDescriptor>());
			}
			nameToLocations.get(fieldName).add(
					new LocationDescriptor(programName,field.getLineNumber(), field.getColumnNumber()));
		}

		// Generate errors for each duplicate
		for (String fieldName : nameToLocations.keySet()) {
			List<LocationDescriptor> locs = nameToLocations.get(fieldName); 
			if (locs.size() > 1) {
				this.errors.add(new DeclaredTwiceSemanticError(type, locs,
						programName, fieldName, methodName));
			}
		}
	}
}
