package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public class TwoSampleStrandedIterator extends AbstractTwoSampleIterator {

	public TwoSampleStrandedIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			AbstractParameters parameters) {
		super(annotatedCoordinate, filter, readersA, readersB, sampleA, sampleB, parameters);
	}

	@Override
	public boolean hasNext() {
		while (hasNextA() && hasNextB()) {
			final int compare = new Integer(locationA.genomicPosition).compareTo(locationB.genomicPosition);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(locationB, pileupBuildersA);
				locationA.genomicPosition = locationB.genomicPosition;
				locationA.strand = locationB.strand;
				break;

			case 0:
				if (locationA.strand != STRAND.UNKNOWN && locationB.strand != STRAND.UNKNOWN && locationA.strand != locationB.strand) {
					locationA.strand = STRAND.REVERSE;
					locationB.strand = STRAND.REVERSE;
					if (! isCovered(locationA, pileupBuildersA) || ! isCovered(locationB, pileupBuildersB)) {
						advance();
						break;
					}
				}

				parallelPileup.setContig(coordinate.getSequenceName());
				parallelPileup.setPosition(locationA.genomicPosition);

				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(locationA, pileupBuildersA));
				if(locationA.strand == STRAND.UNKNOWN && locationB.strand == STRAND.REVERSE) {
					parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
				}
				parallelPileup.setPileupsB(getPileups(locationB, pileupBuildersB));
				if(locationB.strand == STRAND.UNKNOWN && locationA.strand == STRAND.REVERSE) {
					parallelPileup.setPileupsB(complementPileups(parallelPileup.getPileupsB()));
				}

				// 
				if(locationA.strand == STRAND.REVERSE && locationB.strand == STRAND.REVERSE) {
					parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
					parallelPileup.setPileupsB(complementPileups(parallelPileup.getPileupsB()));
				}

				if (filter.isValid(parallelPileup)) {
					return true;
				} else {
					advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(locationA, pileupBuildersB);
				locationB.genomicPosition = locationA.genomicPosition;
				locationB.strand = locationA.strand;
				break;
			}
		}

		return false;
	}

	@Override
	public ParallelPileup next() {
		/*
		if (! hasNext()) {
			return null;
		}
		*/

		if (filterconfig.hasFiters()) {
			parallelPileup.setFilterCountsA(getCounts(locationA, pileupBuildersA));
			parallelPileup.setFilterCountsB(getCounts(locationB, pileupBuildersB));
		}

		// advance to the next position
		advance();

		return parallelPileup;
	}

	@Override
	protected void advance() {
		if (locationA.strand == STRAND.UNKNOWN) {
			if (locationB.strand == STRAND.UNKNOWN || locationB.strand == STRAND.REVERSE) {
				++locationA.genomicPosition;
				++locationB.genomicPosition;
			} else if (locationB.strand == STRAND.FORWARD){
				locationB.strand = STRAND.REVERSE;
			}
		}

		if (locationB.strand == STRAND.UNKNOWN) {
			if (locationA.strand == STRAND.REVERSE) {
				++locationA.genomicPosition;
				++locationB.genomicPosition;
			} else if (locationA.strand == STRAND.FORWARD){
				locationA.strand = STRAND.REVERSE;
			}
		}

		if (locationA.strand == STRAND.FORWARD && locationB.strand == STRAND.FORWARD) {
			locationA.strand = STRAND.REVERSE;
			locationB.strand = STRAND.REVERSE;
		} else {
			++locationA.genomicPosition;
			++locationB.genomicPosition;

			locationA.strand = STRAND.FORWARD;
			locationB.strand = STRAND.FORWARD;
		}
	}

	@Override
	protected void advance(Location location) {
		switch (location.strand) {
		case FORWARD:
			location.strand = STRAND.REVERSE;
			break;

		case REVERSE:
			++location.genomicPosition;
			location.strand = STRAND.FORWARD;
			break;

		case UNKNOWN:
		default:
			++location.genomicPosition;
			break;
		}
	}

}