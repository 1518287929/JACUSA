package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.WindowedParallelPileup;
import accusa2.pileup.WindowedPileup;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

// TODO implement finalize
public class TwoSampleWindowedIterator extends AbstractWindowIterator {

	private BaseConfig baseConfig;
	//private TwoSampleUnstrandedIterator unstrandedIterator;
	private WindowedParallelPileup windowedParallelPileup;

	public TwoSampleWindowedIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final Variant filter,
			final SAMFileReader[] readersA, 
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, filter, parameters);

		this.baseConfig = parameters.getBaseConfig();
		//unstrandedIterator = new TwoSampleUnstrandedIterator(annotatedCoordinate, filter, readersA, readersB, sampleA, sampleB, parameters);

		windowedParallelPileup = new WindowedParallelPileup(null, null); // TODO
	}

	protected boolean hasNext(Location location, final AbstractPileupBuilder[] pileupBuilders) {
		return false; // TODO check what to do
	}

	@Override
	protected WindowedPileup[] getPileups(Location location, AbstractPileupBuilder[] pileupBuilders) {
		int replicates = pileupBuilders.length;
		
		WindowedPileup[] pileups = new WindowedPileup[replicates];
		for(int replicate = 0; replicate < replicates; ++replicate) {
			pileups[replicate] = new WindowedPileup(baseConfig);
		}

		for (; location.genomicPosition < getAnnotatedCoordinate().getEnd(); ++location.genomicPosition) {
			int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(location.genomicPosition);
			for(int replicate = 0; replicate < replicates; ++replicate) {
				pileups[replicate].addPileup(pileupBuilders[replicate].getPileup(windowPosition, location.strand));
			}
		}

		return pileups;
	}

	@Override
	protected Counts[][] getCounts(Location location, AbstractPileupBuilder[] pileupBuilders) {
		return null;
	}

	public boolean hasNext() {
		return false;
	}

	public ParallelPileup next() {
		// advance to the next position
		advance();

		return windowedParallelPileup;
	}

	protected void advance() {
		// not needed
	}

	@Override
	protected void advance(Location location) {
		// not needed
	}

}