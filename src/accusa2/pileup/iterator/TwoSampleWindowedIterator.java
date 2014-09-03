package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.WindowedPileup;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleWindowedIterator extends AbstractTwoSampleIterator {

	public TwoSampleWindowedIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader[] readersA, 
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, readersA, readersB, sampleA, sampleB, parameters);
	}

	protected boolean hasNextA() {
		return true;
	}

	protected boolean hasNextB() {
		return true;
	}

	protected int advance(int currentGenomicPosition, STRAND strand) {
		return currentGenomicPosition;
	}

	protected int hasNext(final int currentGenomicPosition, STRAND strand, final AbstractPileupBuilder[] pileupBuilders) {
		return -1;
	}

	@Override
	protected WindowedPileup[] getPileups(int genomicPosition, STRAND strand, AbstractPileupBuilder[] pileupBuilders) {
		int replicates = pileupBuilders.length;
		
		WindowedPileup[] pileups = new WindowedPileup[replicates];
		for(int replicate = 0; replicate < replicates; ++replicate) {
			pileups[replicate] = new WindowedPileup();
		}

		for (; genomicPosition < getAnnotatedCoordinate().getEnd(); ++genomicPosition) {
			int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
			for(int replicate = 0; replicate < replicates; ++replicate) {
				pileups[replicate].addPileup(pileupBuilders[replicate].getPileup(windowPosition, strand));
			}
		}

		return pileups;
	}

	// FIXME currently no filtering
	@Override
	protected Counts[][] getCounts(int genomicPosition, STRAND strand, AbstractPileupBuilder[] pileupBuilders) {
		/*
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterCount];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuilders[i].getFilteredCounts(windowPosition, strand);
		}
		 */
		return null;
	}

	public boolean hasNext() {

		return false;
	}

	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		// FIXME currently no filtering
		// parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandA, pileupBuildersA));
		// parallelPileup.setFilterCountsB(getCounts(genomicPositionB, strandB, pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	protected void advance() {
		genomicPositionA++;
		genomicPositionB++;
	}

}