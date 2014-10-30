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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + colNo;
        result = prime * result
                + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + lineNo;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocationDescriptor)) {
            return false;
        }
        LocationDescriptor other = (LocationDescriptor) obj;
        if (colNo != other.colNo) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (lineNo != other.lineNo) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "file \"" + fileName + "\" at line " + lineNo + ", column " + colNo;
    }
}
