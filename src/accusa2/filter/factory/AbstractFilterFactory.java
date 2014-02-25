package accusa2.filter.factory;

import accusa2.cli.Parameters;
import accusa2.filter.process.AbstractParallelPileupFilter;
import accusa2.filter.process.AbstractPileupBuilderFilter;

public abstract class AbstractFilterFactory {

	public final static char SEP = ':';

	private char c;
	private String desc;

	private Parameters parameters;

	public AbstractFilterFactory(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public abstract AbstractParallelPileupFilter getParallelPileupFilterInstance();
	public abstract AbstractPileupBuilderFilter getPileupBuilderFilterInstance();

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	public final Parameters getParameters() {
		return parameters;
	}

	public final void setParameters(final Parameters parameters) {
		this.parameters = parameters;
	}

	public void processCLI(final String line) throws IllegalArgumentException {
		// implement to change behavior via CLI
	}

}
