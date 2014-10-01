package edu.mit.semantics.errors;

public class LocationInFile {
	final int lineNumber;
	final int columnNumber;
	
	public LocationInFile(int line, int column) {
		this.lineNumber = line;
		this.columnNumber = column;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}
}
