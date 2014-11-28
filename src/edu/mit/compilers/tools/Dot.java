package edu.mit.compilers.tools;

/** Utilities for generating DOT graphs. */
public class Dot {
    private Dot() {}

    public static String node(long nodeId, String label) {
        // We prepend a backslash to any backslash or quote.
        // We need four backslashes to make it propogate through the Java String literal definition
        // and through the regex system.
        String escapedBackslashes = label.replaceAll("(\\\\)", "\\\\$1");
        String escapedQuotes = escapedBackslashes.replaceAll("(\")","\\\\$1");
        return nodeId + "[label=\"" + escapedQuotes + "\"];";
    }

    public static String edge(long sourceId, long destinationId) {
        return sourceId + " -> " + destinationId + ";";
    }

    public static String labeledEdge(long sourceId, long destinationId, String label) {
        return String.format("%d -> %d [taillabel = \"%s\"];", sourceId, destinationId, label);
    }
}
