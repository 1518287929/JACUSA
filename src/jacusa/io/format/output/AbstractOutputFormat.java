package jacusa.io.format.output;

import jacusa.pileup.ParallelPileup;

public abstract class AbstractOutputFormat {

	private char c;
	private String desc;
	
	public AbstractOutputFormat(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	// Header is empty by default
	public String getHeader(ParallelPileup parallelPileup) {
		return null;
	}
	
	public abstract String convert2String(ParallelPileup parallelPileup);

	public abstract char getCOMMENT();
	public abstract char getSEP();
	public abstract char getSEP2();
	public abstract char getEMPTY();
	
}