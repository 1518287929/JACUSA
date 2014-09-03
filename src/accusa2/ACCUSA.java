package accusa2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.CLI;
import accusa2.method.AbstractMethodFactory;
import accusa2.method.call.OneSampleCallFactory;
import accusa2.method.call.TwoSampleCallFactory;
import accusa2.method.pileup.TwoSamplePileupFactory;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractWorker;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.BedCoordinateProvider;
import accusa2.util.CoordinateProvider;
import accusa2.util.SimpleTimer;

/**
 * @author Michael Piechotta
 */
public class ACCUSA {

	// timer used for all time measurements
	private static SimpleTimer timer;
	public static final String VERSION = "2.99";
	
	// command line interface
	private CLI cli;

	/**
	 * 
	 */
	public ACCUSA() {
		cli = CLI.getSingleton();

		// container for available methods (e.g.: call, pileup)
		Map<String, AbstractMethodFactory> methodFactories = new TreeMap<String, AbstractMethodFactory>();

		AbstractMethodFactory[] factories = new AbstractMethodFactory[] {new OneSampleCallFactory(), new TwoSampleCallFactory(), new TwoSamplePileupFactory()};
		for (AbstractMethodFactory factory : factories) {
			methodFactories.put(factory.getName(), factory);
		}
		
		// add to cli 
		cli.setMethodFactories(methodFactories);
	}

	/**
	 * Singleton Pattern
	 * @return a SimpleTimer instance
	 */
	public static SimpleTimer getSimpleTimer() {
		if(timer == null) {
			timer = new SimpleTimer();
		}

		return timer;
	}

	/**
	 * 
	 * @return
	 */
	public CLI getCLI() {
		return cli;
	}

	/**
	 * 
	 * @param pathnamesA
	 * @param pathnamesB
	 * @return
	 * @throws Exception
	 */
	public List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnamesA, String[] pathnamesB) throws Exception {
		printLog("Computing overlap between sequence records.");
		String error = "Sequence Dictionary of BAM files do not match";

		SAMFileReader reader 				= new SAMFileReader(new File(pathnamesA[0]));
		List<SAMSequenceRecord> records 	= reader.getFileHeader().getSequenceDictionary().getSequences();
		// close readers
		reader.close();

		List<AnnotatedCoordinate> coordinates = new ArrayList<AnnotatedCoordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for (SAMSequenceRecord record : records) {
			coordinates.add(new AnnotatedCoordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if (! isValid(targetSequenceNames, pathnamesA) || !isValid(targetSequenceNames, pathnamesB)) {
			throw new Exception(error);
		}

		return records;
	}

	private boolean isValid(Set<String> targetSequenceNames, String[] pathnames) {
		Set<String> sequenceNames = new HashSet<String>();
		for (String pathname : pathnames) {
			SAMFileReader reader = new SAMFileReader(new File(pathname));
			List<SAMSequenceRecord> records	= reader.getFileHeader().getSequenceDictionary().getSequences();
			for (SAMSequenceRecord record : records) {
				sequenceNames.add(record.getSequenceName());
			}	
			reader.close();
		}
		
		if (! sequenceNames.containsAll(targetSequenceNames) || !targetSequenceNames.containsAll(sequenceNames)) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param size
	 * @param args
	 */
	public void printProlog(String[] args) {
		String lineSep = "--------------------------------------------------------------------------------";

		System.err.println(lineSep);

		StringBuilder sb = new StringBuilder();
		sb.append("ACCUSA25");
		for(String arg : args) {
			sb.append(" " + arg);
		}
		System.err.println(sb.toString());

		System.err.println(lineSep);
	}

	/**
	 * 
	 * @param line
	 */
	public static void printLog(String line) {
		String time = "[ INFO ] " + getSimpleTimer().getTotalTimestring() + ": ";
		System.err.println(time + " " + line);
	}

	/**
	 * 
	 * @param comparisons
	 */
	public void printEpilog(int comparisons) {
		// print statistics to STDERR
		printLog("Screening done using " + cli.getMethodFactory().getParameters().getMaxThreads() + " thread(s)");

		System.err.println("Results can be found in: " + cli.getMethodFactory().getParameters().getOutput().getInfo());

		String lineSep = "--------------------------------------------------------------------------------";

		System.err.println(lineSep);
		System.err.println("Analyzed Parallel Pileups:\t" + comparisons);
		System.err.println("Elapsed time:\t\t\t" + getSimpleTimer().getTotalTimestring());
	}

	/**
	 * Application logic.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ACCUSA accusa = new ACCUSA();
		CLI cmd = accusa.getCLI();

		if (! cmd.processArgs(args)) {
			System.exit(1);
		}

		AbstractMethodFactory methodFactory = cmd.getMethodFactory();
		AbstractParameters parameters = methodFactory.getParameters();
		
		CoordinateProvider coordinateProvider = null;
		if (parameters.getBedPathname().isEmpty()) {
			methodFactory.initCoordinateProvider();
			coordinateProvider = methodFactory.getCoordinateProvider();
		} else {
			coordinateProvider = new BedCoordinateProvider(parameters.getBedPathname());
		}

		// prolog
		accusa.printProlog(args);
		// main
		AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher = methodFactory.getInstance(coordinateProvider);
		int comparisons = workerDispatcher.run();
		// epilog
		accusa.printEpilog(comparisons);

		// cleaup
		parameters.getOutput().close();
	}

}