package jacusa.cli.parameters;

import jacusa.filter.FilterConfig;
import jacusa.io.Output;
import jacusa.io.OutputPrinter;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.builder.PileupBuilderFactory;
import jacusa.pileup.builder.UndirectedPileupBuilderFactory;

public abstract class AbstractParameters implements hasSampleA {
	
	// cache related
	private int windowSize;

	private BaseConfig baseConfig;

	private int maxThreads;

	// bed file to scan for variants
	private String bedPathname;

	// chosen method
	private AbstractMethodFactory methodFactory;

	private SampleParameters sampleA;

	private Output output;
	private AbstractOutputFormat format;
	private FilterConfig filterConfig;

	// debug flag
	private boolean debug;

	public AbstractParameters() {
		windowSize 	= 10000;
		baseConfig	= new BaseConfig(BaseConfig.VALID);

		maxThreads	= 1;
		
		bedPathname	= new String();
		sampleA		= new SampleParameters();
		
		output		= new OutputPrinter();
		filterConfig= new FilterConfig();
		
		debug		= false;
	}

	public AbstractOutputFormat getFormat() {
		return format;
	}

	public void setFormat(AbstractOutputFormat format) {
		this.format = format;
	}
	
	/**
	 * @return the filterConfig
	 */
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}
	
	/**
	 * @return the output
	 */
	public Output getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(Output output) {
		this.output = output;
	}

	/**
	 * @return the sampleA
	 */
	public SampleParameters getSample1() {
		return sampleA;
	}
	
	/**
	 * @return the baseConfig
	 */
	public BaseConfig getBaseConfig() {
		return baseConfig;
	}
	
	protected PileupBuilderFactory getDefaultPileupBuilderFactory() {
		return new UndirectedPileupBuilderFactory();
	}
	
	/**
	 * @return the windowSize
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * @param windowSize the windowSize to set
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * @return the maxThreads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * @param maxThreads the maxThreads to set
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * @return the bedPathname
	 */
	public String getBedPathname() {
		return bedPathname;
	}

	/**
	 * @param bedPathname the bedPathname to set
	 */
	public void setBedPathname(String bedPathname) {
		this.bedPathname = bedPathname;
	}

	/**
	 * @return the methodFactory
	 */
	public AbstractMethodFactory getMethodFactory() {
		return methodFactory;
	}

	/**
	 * @param methodFactory the methodFactory to set
	 */
	public void setMethodFactory(AbstractMethodFactory methodFactory) {
		this.methodFactory = methodFactory;
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}