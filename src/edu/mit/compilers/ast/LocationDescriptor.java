package edu.mit.compilers.ast;

public class LocationDescriptor {

	private final String fileName;
	private final int lineNo;
	private final int colNo;
	
	public LocationDescriptor(String fileName, int lineNo, int colNo){
		this.fileName = fileName;
		this.lineNo = lineNo;
		this.colNo = colNo;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public int lineNo(){
		return lineNo;
	}
	
	public int colNo(){
		return colNo;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == null){
			return false;
		} else if (!this.getClass().equals(other)) {
			return false;
		} else {
			return ((LocationDescriptor) other).getFileName().equals(this.getFileName()) &&
				   ((LocationDescriptor) other).colNo() == this.colNo() &&
				   ((LocationDescriptor) other).lineNo() == this.lineNo();
		}
	}

    @Override
    public String toString() {
        return "file \"" + fileName + "\" at line " + lineNo + ", column " + colNo;
    }
}
