package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.mit.compilers.ast.*;
import edu.mit.semantics.errors.DeclaredTwiceSemanticError;
import edu.mit.semantics.errors.LocationType;
import edu.mit.semantics.errors.SemanticError;

/**
 * Checks for the following rule/s:
 *
 * 1) No identifier is declared twice in the same scope.
 * This includes 'callout' identifiers, which exist in the global scope.
 * 
 * TODO(Manny): refactor for FieldDescriptors now having LocationDescriptors
 */
public class DeclaredTwiceSemanticCheck implements SemanticCheck {
    private List<SemanticError> errors = new ArrayList<SemanticError>();
    Program prog;

    public DeclaredTwiceSemanticCheck(Program prog) {
        this.prog = prog;
    }

    public List<SemanticError> doCheck() {
        checkCalloutsAndGlobals(, prog.getMethods(),
                prog.getCallouts(), prog.getGlobals());
        checkMethods(prog.getMethods());

        return this.errors;
    }

    /**
     * Given the methods, callouts and globals, compares the names of all three
     * groups together and generates errors for duplicate names.
     */
    private void checkCalloutsAndGlobals(NodeSequence<Method> methods,
            NodeSequence<Callout> callouts, Scope globals) {
        HashMap<String, List<LocationDescriptor>> nameToLocations =
                new HashMap<String, List<LocationDescriptor>>();

        // Get names of methods
        processMethodNames(methods,nameToLocations);

        // Get names of callouts
        processCallouts(callouts,nameToLocations);

        // Get names of globals
        processScope(globals, nameToLocations);

        // Generate errors for each duplicate
        generateDuplicateErrors(nameToLocations, LocationType.GLOBAL);
    }

    /**
     * Checks each scope in each method for variables with the same name.
     * The parameters and immediate local fields are considered in the same
     * scope.
     */
    private void checkMethods(NodeSequence<Method> methods) {
        Iterable<Method> methodItr = (Iterable<Method>) methods.getChildren();

        // Iterate through each method
        for (Method method : methodItr) {
            checkMethod(method.getName(), method.getParameters(),
                    method.getBlock());
        }
    }

    /**
     * Checks immediate local scope and parameter scope together for
     * duplicates. Explores statements for more blocks.
     */
    private void checkMethod(String methodName, Scope paramScope,
            Block block) {
        HashMap<String, List<LocationDescriptor>> nameToLocations = new HashMap<String, List<LocationDescriptor>>();

        // Get names and locations
        processScope(paramScope, nameToLocations);
        processScope(block.getScope(), nameToLocations);

        // Generate errors for duplicates
        generateDuplicateErrors(nameToLocations, LocationType.PARAM, methodName);

        // Check sublocal fields
        checkBlock(block,methodName);
    }

    /**
     * Checks for duplicates in the subscopes of the block.
     * 
     * Does not check the direct scope of the block.
     */
    private void checkBlock(Block block, String methodName) {
        // Go through each Statement in a Block and find subblocks
        Iterable<Statement> statements = block.getStatements();
        for (Statement statement : statements) {
            // For each subblock, perform the semantic check and recurse
            Iterable<Block> subblocks = statement.getBlocks();

            for (Block subblock : subblocks) {
                checkScope(LocationType.LOCAL,
                        subblock.getScope(),methodName);
                checkBlock(subblock, methodName);
            }
        }
    }

    /**
     * Checks unique field names in a scope. General enough for all scopes,
     * but intended for sublocal scopes.
     */
    private void checkScope(LocationType type, Scope scope,
            String methodName){
        HashMap<String, List<LocationDescriptor>> nameToLocations = new HashMap<String, List<LocationDescriptor>>();

        // Get duplicates and record line numbers each field goes to
        processScope(scope, nameToLocations);

        // Generate errors for each duplicate
        generateDuplicateErrors(nameToLocations, type, methodName);
    }

    /**
     * Takes all the method names and their locations and processes them into
     * the hashmap.
     */
    private void processMethodNames(NodeSequence<Method> methods,
            HashMap<String, List<LocationDescriptor>> n2locs) {
        Iterable<Method> methodItr = (Iterable<Method>) methods.getChildren();

        for (Method method : methodItr) {
            String name = method.getName();
            if (!n2locs.containsKey(name)) {
                n2locs.put(name, new ArrayList<LocationDescriptor>());
            }
            n2locs.get(name).add(method.getLocationDescriptor());
        }
    }

    /**
     * Takes all the callout names and their locations and processes them into
     * the hashmap.
     */
    private void processCallouts(NodeSequence<Callout> callouts,
            HashMap<String, List<LocationDescriptor>> n2locs) {
        Iterable<Callout> calloutItr = (Iterable<Callout>) callouts.getChildren();

        for (Callout callout : calloutItr) {
            String name = callout.getName();
            if (!n2locs.containsKey(name)) {
                n2locs.put(name, new ArrayList<LocationDescriptor>());
            }
            n2locs.get(name).add(callout.getLocationDescriptor());
        }
    }

    /**
     * Takes all the names of a scope and their locations and processes them into
     * the hashmap.
     */
    private void processScope(Scope scope, 
            HashMap<String, List<LocationDescriptor>> n2locs){
        List<FieldDescriptor> fields = scope.getVariables();

        // Add the locations of each name to the hashmap
        for (FieldDescriptor field : fields) {
            String fieldName = field.getName();

            if (!n2locs.containsKey(fieldName)) {
                n2locs.put(fieldName, new ArrayList<LocationDescriptor>());
            }
            n2locs.get(fieldName).add(field.getLocationDescriptor());
        }
    }

    /**
     * Checks which names have been used multiple times in the global scope.
     * 
     * Helper function for globals, which do not have a methodName for the
     * full generateDuplucateErrors function.
     */
    private void generateDuplicateErrors(HashMap<String,
            List<LocationDescriptor>> nameToLocations, LocationType type) {
        generateDuplicateErrors(nameToLocations,type, null);
    }

    /**
     * Checks which names have been used multiple times and
     * generates errors for those names.
     * 
     * The rest of the parameters are necessary for the generation of the
     * error message.
     */
    private void generateDuplicateErrors(HashMap<String,
            List<LocationDescriptor>> nameToLocations, LocationType type,
            String methodName) {
        for (String fieldName : nameToLocations.keySet()) {
            List<LocationDescriptor> locs = nameToLocations.get(fieldName);
            if (locs.size() > 1) {
                this.errors.add(new DeclaredTwiceSemanticError(type, locs,
                        this.prog.getName(), fieldName, methodName));
            }
        }
    }
}
