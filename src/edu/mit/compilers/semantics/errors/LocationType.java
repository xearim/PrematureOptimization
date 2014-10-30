package edu.mit.compilers.semantics.errors;

public enum LocationType {
    GLOBAL ("global"), // includes callouts as well as global fields
    PARAM ("parameter and immediate local scope"),
    LOCAL ("sublocal");

    private final String description;

    private LocationType(String desc) {
        this.description = desc;
    }

    public String toString() {
        return description;
    }
}
