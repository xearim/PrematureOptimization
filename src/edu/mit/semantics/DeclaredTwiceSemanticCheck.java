package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.compilers.ast.*;
import edu.mit.semantics.errors.DeclaredTwiceSemanticError;
import edu.mit.semantics.errors.LocationType;
import edu.mit.semantics.errors.SemanticError;

/**
 * Checks for the following rule/s:
 *
 * 1) No identifier is declared twice in the same scope.
 * This includes 'callout' identifiers, which exist in the global scope.
 * Parameters and immediate local variables are included in the same scope.
 */
public class DeclaredTwiceSemanticCheck implements SemanticCheck {
    private List<SemanticError> errors = new ArrayList<SemanticError>();
    Program prog;

    public DeclaredTwiceSemanticCheck(Program prog) {
        this.prog = prog;
    }

    public List<SemanticError> doCheck() {
        checkCalloutsAndGlobals(prog.getMethods(),
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
        Map<String, List<LocationDescriptor>> nameToLocations =
                new HashMap<String, List<LocationDescriptor>>();

        /*
         * Map names to lists of location descriptors. Append each location
         * descriptor to the list mapped to that name.
         */ 
        processMethodNames(methods,nameToLocations);
        processCallouts(callouts,nameToLocations);
        processScope(globals, nameToLocations);

        /*
         * Generate errors if for each name that maps to a list with multiple
         * location descriptors
         */
        generateDuplicateErrors(nameToLocations, LocationType.GLOBAL);
    }

    /**
     * Checks each scope in each method for variables with the same name.
     * The parameters and immediate local fields are considered in the same
     * scope.
     */
    private void checkMethods(NodeSequence<Method> methods) {
        // Iterate through each method
        for (Method method : methods.getChildren()) {
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
        Map<String, List<LocationDescriptor>> nameToLocations =
                new HashMap<String, List<LocationDescriptor>>();

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
        /*
         * Go through each Statement in a Block and find subblocks. For each
         * subblock, perform the semantic check and recurse.
         */
        for (Statement statement : block.getStatements()) {
            /*
             * Three cases for statements:
             * 1) methodcall - need to check if the method's name has been
             *      overshadowed.
             * 2) if,while,for - these have blocks that need to be recursed on
             * 3) all else - nothing
             */
            if (statement instanceof MethodCall) {
                String methodCallName = ((MethodCall) statement).getMethodName();
                Scope scope = block.getScope();
                // Recurse on all scopes until we hit the global scope
                while (scope.getParent().isPresent()) {
                    if (scope.isInScope(methodCallName)) {
                        /*
                         * If overshadowed, get the location of the
                         * overshadowing variable declaration.
                         */
                        LocationDescriptor overshadowLocation =
                                scope.getFromScope(methodCallName).get().getLocationDescriptor();

                        // Accumulate the error, already know the error exists
                        Utils.check(true, errors,
                                "Invalid method call: method %s called at %s but was overshadowed at %s. ",
                                methodCallName,
                                ((MethodCall) statement).getLocationDescriptor(),
                                overshadowLocation);
                    }
                }
            }
            else {
                for (Block subblock : statement.getBlocks()) {
                    checkScope(LocationType.LOCAL,subblock.getScope(),methodName);
                    checkBlock(subblock, methodName);
                }
            }
        }
    }

    /**
     * Checks unique field names in a scope. General enough for all scopes,
     * but intended for sublocal scopes.
     */
    private void checkScope(LocationType type, Scope scope,
            String methodName){
        Map<String, List<LocationDescriptor>> nameToLocations =
                new HashMap<String, List<LocationDescriptor>>();

        /*
         * Map names to lists of location descriptors. Append each location
         * descriptor to the list mapped to that name.
         */ 
        processScope(scope, nameToLocations);

        // Generate errors for each duplicate
        generateDuplicateErrors(nameToLocations, type, methodName);
    }

    /**
     * For each method name, appends the LocationDescriptor of that method to a
     * list that has been hashed to the method name.
     *
     * @param nameToLocations accumulates the LocationDescriptors for each
     * name
     */
    private void processMethodNames(NodeSequence<Method> methods,
            Map<String, List<LocationDescriptor>> nameToLocations) {
        for (Method method : methods.getChildren()) {
            String name = method.getName();
            if (!nameToLocations.containsKey(name)) {
                nameToLocations.put(name, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(name).add(method.getLocationDescriptor());
        }
    }

    /**
     * For each callout name, appends the LocationDescriptor of that callout to
     * a list that has been hashed to the callout name.
     *
     * @param nameToLocations accumulates the LocationDescriptors for each
     * name.
     */
    private void processCallouts(NodeSequence<Callout> callouts,
            Map<String, List<LocationDescriptor>> nameToLocations) {

        for (Callout callout : callouts.getChildren()) {
            String name = callout.getName();
            if (!nameToLocations.containsKey(name)) {
                nameToLocations.put(name, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(name).add(callout.getLocationDescriptor());
        }
    }

    /**
     * For each field name in the scope, appends the LocationDescriptor of
     * that variable to a list that has been hashed to the field name.
     *
     * @param nameToLocations accumulates the LocationDescriptors for each
     * name.
     */
    private void processScope(Scope scope,
            Map<String, List<LocationDescriptor>> nameToLocations){
        // Add the locations of each name to the hashmap
        for (FieldDescriptor field : scope.getVariables()) {
            String fieldName = field.getName();

            if (!nameToLocations.containsKey(fieldName)) {
                nameToLocations.put(fieldName, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(fieldName).add(field.getLocationDescriptor());
        }
    }

    /**
     * Checks which names have been used multiple times in the global scope.
     *
     * Helper function for globals, which do not have a methodName for the
     * full generateDuplicateErrors function.
     */
    private void generateDuplicateErrors(Map<String,
            List<LocationDescriptor>> nameToLocations, LocationType type) {
        generateDuplicateErrors(nameToLocations,type, null);
    }

    /**
     * Checks which names have been used multiple times and
     * generates errors for those names.
     * 
     * The rest of the parameters are necessary for the generation of the
     * error message.
     *
     * @param nameToLocations gets duplicates by checking this parameter.
     * @param type specifies if global, parameter, or local
     * @param methodName name of the method that these names were extracted
     * from. It expects a null if in the global scope
     */
    private void generateDuplicateErrors(Map<String,
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
