package jacusa.method.pileup;


import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
import jacusa.cli.options.DebugOption;
import jacusa.cli.options.FormatOption;
import jacusa.cli.options.HelpOption;
import jacusa.cli.options.MaxDepthOption;
import jacusa.cli.options.MaxThreadOption;
import jacusa.cli.options.MinBASQOption;
import jacusa.cli.options.MinCoverageOption;
import jacusa.cli.options.MinMAPQOption;
import jacusa.cli.options.PathnameArg;
import jacusa.cli.options.ResultFileOption;
import jacusa.cli.options.VersionOption;
import jacusa.cli.options.WindowSizeOption;
import jacusa.cli.options.filter.FilterFlagOption;
import jacusa.cli.options.pileupbuilder.TwoSamplePileupBuilderOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSamplePileupParameters;
import jacusa.io.format.output.AbstractOutputFormat;
import jacusa.io.format.output.PileupFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.process.parallelpileup.dispatcher.pileup.MpileupWorkerDispatcher;
import jacusa.util.coordinateprovider.CoordinateProvider;
import jacusa.util.coordinateprovider.SAMCoordinateProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.sf.samtools.SAMSequenceRecord;

public class TwoSamplePileupFactory extends AbstractMethodFactory {

	private static MpileupWorkerDispatcher instance;
	private TwoSamplePileupParameters parameters;

	public TwoSamplePileupFactory() {
		super("pileup", "SAMtools like mpileup for two samples");

		parameters = new TwoSamplePileupParameters();
	}

	public void initACOptions() {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		SampleParameters[] samples = new SampleParameters[] {
			sample1, sample2
		};
		
		// global settings
		acOptions.add(new MinMAPQOption(samples));
		acOptions.add(new MinBASQOption(samples));
		acOptions.add(new MinCoverageOption(samples));
		acOptions.add(new MaxDepthOption(samples));
		acOptions.add(new FilterFlagOption(samples));

		acOptions.add(new TwoSamplePileupBuilderOption(sample1, sample2));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		
		if (getOuptutFormats().size() == 1 ) {
			Character[] a = getOuptutFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getOuptutFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption<AbstractOutputFormat>(parameters, getOuptutFormats()));
		}

		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new WindowSizeOption(parameters));

		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	public Map<Character, AbstractOutputFormat> getOuptutFormats() {
		Map<Character, AbstractOutputFormat> outputFormats = new HashMap<Character, AbstractOutputFormat>();

		AbstractOutputFormat outputFormat = new PileupFormat(parameters.getBaseConfig());
		outputFormats.put(outputFormat.getC(), outputFormat);

		return outputFormats;
	}

	@Override
	public MpileupWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) {
		if(instance == null) {
			instance = new MpileupWorkerDispatcher(coordinateProvider, parameters);
		}

		return instance;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnames1 = parameters.getSample1().getPathnames();
		String[] pathnames2 = parameters.getSample2().getPathnames();

		List<SAMSequenceRecord> records = getSAMSequenceRecords(pathnames1, pathnames2);
		coordinateProvider = new SAMCoordinateProvider(records);
	}

	@Override
	public AbstractParameters getParameters() {
		return parameters;
	}
	

	@Override
	public boolean parseArgs(String[] args) throws Exception {
		if (args == null || args.length != 2) {
			throw new ParseException("BAM File is not provided!");
		}

		PathnameArg pa = new PathnameArg(1, parameters.getSample1());
		pa.processArg(args[0]);
		pa = new PathnameArg(2, parameters.getSample2());
		pa.processArg(args[1]);

		return true;
	}
	
	@Override
	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(160);

		Set<AbstractACOption> acOptions = getACOptions();
		Options options = new Options();
		for (AbstractACOption acoption : acOptions) {
			options.addOption(acoption.getOption());
		}

		formatter.printHelp(JACUSA.NAME + " [OPTIONS] BAM1_1[,BAM1_2,BAM1_3,...] BAM2_1[,BAM2_2,BAM2_3,...]", options);
	}
	
}
