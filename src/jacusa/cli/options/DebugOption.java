package jacusa.cli.options;

import jacusa.cli.parameters.AbstractParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class DebugOption extends AbstractACOption {

	private AbstractParameters parameters;
	
	public DebugOption(AbstractParameters parameters) {
		this.parameters = parameters;

		opt = "D";
		longOpt = "debug";
	}
	
	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
			parameters.setDebug(true);
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(false)
		        .withDescription("Enable debug modus")
		        .create(opt);
	}

}