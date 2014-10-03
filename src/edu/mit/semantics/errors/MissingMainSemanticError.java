package edu.mit.semantics.errors;


public class MissingMainSemanticError implements SemanticError {
    private final static String ERRORNAME= "MissingMainSemanticError";

    @Override
    public String generateErrorMessage() {
        // TODO Auto-generated method stub
        return String.format("%s: Missing main method", ERRORNAME);
    }

}
