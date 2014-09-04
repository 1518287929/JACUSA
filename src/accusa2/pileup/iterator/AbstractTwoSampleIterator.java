package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractTwoSampleIterator extends AbstractWindowIterator {

	// sample A
	protected SampleParameters sampleA;
	protected Location locationA;
	protected final AbstractPileupBuilder[] pileupBuildersA;	

	// sample B
	protected SampleParameters sampleB;
	protected Location locationB;
	protected final AbstractPileupBuilder[] pileupBuildersB;
	
	// output
	protected ParallelPileup parallelPileup;
	
	public AbstractTwoSampleIterator(
			final AnnotatedCoordinate annotatedCoordinate,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			AbstractParameters parameters) {
		super(annotatedCoordinate, parameters);

		locationA = new Location(-1, STRAND.UNKNOWN);
		this.sampleA = sampleA;
		pileupBuildersA = createPileupBuilders(
				sampleA.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersA,
				sampleA,
				parameters);
		initLocation(locationA, sampleA.getPileupBuilderFactory().isDirected(), pileupBuildersA);
		
		locationB = new Location(-1, STRAND.UNKNOWN);
		this.sampleB = sampleB;
		pileupBuildersB = createPileupBuilders(
				sampleB.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersB,
				sampleB,
				parameters);
		initLocation(locationB, sampleB.getPileupBuilderFactory().isDirected(), pileupBuildersB);

		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, pileupBuildersB.length);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());
	}

	protected boolean hasNextA() {
		return hasNext(locationA, pileupBuildersA);
	}
	
	protected boolean hasNextB() {
		return hasNext(locationB, pileupBuildersB);
	}
	
}