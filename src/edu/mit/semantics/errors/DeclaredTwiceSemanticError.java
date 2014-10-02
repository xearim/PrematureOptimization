package edu.mit.semantics.errors;

import java.util.List;

import edu.mit.compilers.ast.LocationDescriptor;

/**
 * This class represents an error that violates the first rule:
 * 1) No identifier is declared twice in the same scope.
 * This includes 'callout' identifiers, which exist in the global scope.
 */
public class DeclaredTwiceSemanticError implements SemanticError {
    private static final String GLOBALSCOPE = "global",
            PARAMSCOPE = "parameter", LOCALSCOPE = "local";
    private final LocationType type;
    private final List<LocationDescriptor> locations;
    private final String name,methodName,programName;

    /**
     * Callout needs locations(1), program name, and name of callout
     * Global needs locations, program name, and name of variable
     * Parameter and Local need locations, program name, name of variable and method name
     *
     * (1) Currently callouts don't have locations in the standard way, so they
     * aren't being used.
     * TODO(Manny) add locations to callouts
     */
    public DeclaredTwiceSemanticError(LocationType type,
            List<LocationDescriptor> locs, String progName, String name, String methodName) {
        this.type = type;
        this.locations = locs;
        this.programName = progName;
        this.name = name;
        this.methodName = methodName;
    }

    @Override
    public String generateErrorMessage() {
        String returnString;
        switch (this.type){
            case GLOBAL:
                returnString = String.format("%s in %s: %s variable \"%s\" used multiple times.",
                        this.programName, getLocationsString(), GLOBALSCOPE, this.name);
                break;
            case LOCAL:
                returnString = String.format("%s in %s: %s variable \"%s\" used multiple times in method \"%s\".",
                        this.programName, getLocationsString(), LOCALSCOPE, this.name, this.methodName);
                break;
            case PARAM:
                returnString = String.format("%s in %s: %s variable \"%s\" used multiple times in method \"%s\".",
                        this.programName, getLocationsString(), PARAMSCOPE, this.name, this.methodName);
                break;
            default: // Should never hit this, might want to through exception instead
                returnString = "";
                break;
        }
        return returnString;
    }

    /**
     * If locations recorded are (3,15) and (5,2),
     * returns "3:15; 5:2; "
     *
     * (A,B), A is the line number, B is the column
     */
    private String getLocationsString() {
        String locationsString = "";
        for (LocationDescriptor loc : locations) {
            locationsString += String.format("%d:%d; ", loc.lineNo(), loc.colNo());
        }
        return locationsString;
    }
}
