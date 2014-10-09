package edu.mit.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.*;
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
        checkCalloutsAndGlobals(prog.getMethods(), prog.getCallouts(),
                prog.getGlobals());
        checkMethods(prog.getMethods(), prog.getName());

        return this.errors;
    }

    /**
     * Given the methods, callouts and globals, compares the names of all three
     * groups together and generates errors for duplicate names.
     */
    private void checkCalloutsAndGlobals(NodeSequence<Method> methods,
            NodeSequence<Callout> callouts, Scope globals) {
        HashMap<String, List<LocationDescriptor>> nameToLocations = new HashMap<String, List<LocationDescriptor>>();

        // Get names of methods
        @SuppressWarnings("unchecked")
        Iterable<Method> methodItr = (Iterable<Method>) methods.getChildren();

        for (Method method : methodItr) {
            String name = method.getName();
            if (!nameToLocations.containsKey(name)) {
                nameToLocations.put(name, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(name).add(method.getLocationDescriptor());
        }

        // Get names of callouts
        @SuppressWarnings("unchecked")
        Iterable<Callout> calloutItr = (Iterable<Callout>) callouts.getChildren();

        for (Callout callout : calloutItr) {
            String name = callout.getName();
            if (!nameToLocations.containsKey(name)) {
                nameToLocations.put(name, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(name).add(callout.getLocationDescriptor());
        }

        // Get names of globals
        List<FieldDescriptor> globalItr = globals.getVariables();
        for (FieldDescriptor global : globalItr) {
            String name = global.getName();
            if (!nameToLocations.containsKey(name)) {
                nameToLocations.put(name, new ArrayList<LocationDescriptor>());
            }
            nameToLocations.get(name).add(global.getLocationDescriptor());
        }

        // Generate errors for each duplicate
        for (String fieldName : nameToLocations.keySet()) {
            List<LocationDescriptor> locs = nameToLocations.get(fieldName);
            if (locs.size() > 1) {
                this.errors.add(new DeclaredTwiceSemanticError(LocationType.GLOBAL, locs,
                        this.prog.getName(), fieldName, null));
            }
        }
    }

    /**
     * Checks each scope in each method for variables with the same name.
     * The parameters and immediate local fields are considered in the same
     * scope.
     */
    private void checkMethods(NodeSequence<Method> methods, String programName) {
        /*
         * The cast is necessary, because we need to access elements that are
         * in specifically in Method.
         * TODO(Manny): when node gets a getLocation, fix this
         */
        @SuppressWarnings("unchecked")
        Iterable<Method> methodItr = (Iterable<Method>) methods.getChildren();

        // Iterate through each method
        for (Method method : methodItr) {
//            // Param scope
//            checkScope(LocationType.PARAM, this.prog.getName(),
//                    method.getParameters(), method.getName());
//            // Recurse on Local scope
//            checkBlock(method.getBlock(), method.getName());
            checkMethod(method.getName(), method.getParameters(),
                    method.getBlock(), programName);
//            checKStatements(method.getName(), method.getBlock());
        }
    }
    
    private void checkMethod(String methodName, Scope paramScope,
            Block block, String programName) {
        HashMap<String, List<LocationDescriptor>> nameToLocations = new HashMap<String, List<LocationDescriptor>>();
        
        // Get names of parameters
        
        
        // Get names of immediate
        
        // Generate errors for duplicates
        generateDuplicateErrors(nameToLocations, LocationType.PARAM, programName, methodName);

        
        // Check sublocal fields
        Iterable<Statement> statements = block.getStatements();

        for (Statement statement : statements) {
            Iterable<Block> subblocks = statement.getBlocks();

            for (Block subblock : subblocks) {
                checkBlock(subblock, methodName);
            }
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
     * Checks unique field names in a scope. General enough for all scopes.
     * Intended for parameter and local scopes
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
//        for (String fieldName : nameToLocations.keySet()) {
//            List<LocationDescriptor> locs = nameToLocations.get(fieldName);
//            if (locs.size() > 1) {
//                this.errors.add(new DeclaredTwiceSemanticError(type, locs,
//                        programName, fieldName, methodName));
//            }
//        }
        generateDuplicateErrors(nameToLocations, type, programName, methodName);
    }
    
    /**
     * Takes in a preprocessed hashmap of string to lists of location
     * descriptors. Checks for which names have been used multiple times and
     * generates errors for those names.
     * 
     * The rest of the parameters are necessary for the generation of the
     * error message.
     */
    private void generateDuplicateErrors(HashMap<String,
            List<LocationDescriptor>> nameToLocations, LocationType type,
            String programName, String methodName) {
        for (String fieldName : nameToLocations.keySet()) {
            List<LocationDescriptor> locs = nameToLocations.get(fieldName);
            if (locs.size() > 1) {
                this.errors.add(new DeclaredTwiceSemanticError(type, locs,
                        programName, fieldName, methodName));
            }
        }
    }
}
