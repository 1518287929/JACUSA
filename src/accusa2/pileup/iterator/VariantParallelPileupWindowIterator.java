package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;

import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author Michael Piechotta
 */
public class VariantParallelPileupWindowIterator extends UnstrandedParallelPileupWindowIterator {
	
	public VariantParallelPileupWindowIterator(AnnotatedCoordinate coordinate, SAMFileReader[] readers1, SAMFileReader[] readers2, Parameters parameters) {
		super(coordinate, readers1, readers2, parameters);
	}
	
	// TODO make this more quantitative
	@Override
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}
	
}