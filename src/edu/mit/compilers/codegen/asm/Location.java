package edu.mit.compilers.codegen.asm;

public class Location implements Value {

	private final Register base;
    private final long offset;

    public Location(Register base, Long offset) {
        this.base = base;
        this.offset = offset;
    }
    
    public Register getBase() {
    	return base;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String inAttSyntax() {
        return Long.toString(offset) + "(" + base.inAttSyntax() + ")";
    }
}