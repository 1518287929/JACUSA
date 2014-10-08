package accusa2.filter;

import accusa2.filter.feature.AbstractFilter;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;

public abstract class AbstractCountFilter extends AbstractFilter {

	protected final BaseConfig baseConfig;
	protected final FilterConfig filterConfig;
	protected final int filterI;

	public AbstractCountFilter(final char c, final BaseConfig baseConfig, final FilterConfig filterConfig) {
		super(c);
		this.baseConfig 	= baseConfig;
		this.filterConfig 	= filterConfig;
		filterI				= filterConfig.c2i(c);
	}

	// ORDER RESULTS [0] SHOULD BE THE VARIANT TO TEST AGAINST
	public int[] getVariantBaseIs(final ParallelPileup parallelPileup) {
		final int[] variantBasesIs = parallelPileup.getVariantBaseIs();
		final int[] allelesIs = parallelPileup.getPooledPileup().getAlleles();
		final char refBase = parallelPileup.getPooledPileup().getRefBase();

		// A | G
		// define all non-reference bases as potential variants
		if (DefaultParallelPileup.isHoHo(parallelPileup)) {
			if (refBase == 'N') {
				return new int[0];
			}
			final int refBaseI = baseConfig.getBaseI((byte)refBase);

			// find non-reference base(s)
			int i = 0;
			final int[] tmp = new int[allelesIs.length];
			for (final int baseI : allelesIs) {
				if (baseI != refBaseI) {
					tmp[i] = baseI;
					++i;
				}
			}
			final int[] ret = new int[i];
			System.arraycopy(tmp, 0, ret, 0, i);
			return ret;
		}

		// A | AG
		if (variantBasesIs.length >= 1) {
			return variantBasesIs;
		}

		// AG | AG
		return allelesIs;
	}
	
	/**
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	final protected Pileup[] applyFilter(final int variantBaseI, final Pileup[] pileups, final Counts[][] counts) {
		final Pileup[] filtered = new DefaultPileup[pileups.length];

		boolean processed = false;
		for (int i = 0; i < pileups.length; ++i) {
			filtered[i] = new DefaultPileup(pileups[i]);
			final Counts count = counts[i][filterI];
			if(count != null) { 
				filtered[i].getCounts().substract(variantBaseI, count);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}

	final protected ParallelPileup applyFilter(final int variantBaseI, final ParallelPileup parallelPileup) {
		final Pileup[] filteredPileupsA = applyFilter(variantBaseI, parallelPileup.getPileupsA(), parallelPileup.getFilterCountsA());
		final Pileup[] filteredPileupsB = applyFilter(variantBaseI, parallelPileup.getPileupsB(), parallelPileup.getFilterCountsB());

		if (filteredPileupsA == null && filteredPileupsB == null) {
			return null;
		}

		final ParallelPileup filteredParallelPileup = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
		filteredParallelPileup.setContig(parallelPileup.getContig());
		filteredParallelPileup.setPosition(parallelPileup.getPosition());
		filteredParallelPileup.setStrand(parallelPileup.getStrand());

		if (filteredPileupsA == null) {
			filteredParallelPileup.setPileupsA(parallelPileup.getPileupsA());
		} else {
			filteredParallelPileup.setPileupsA(filteredPileupsA);
		}

		if (filteredPileupsB == null) {
			filteredParallelPileup.setPileupsB(parallelPileup.getPileupsB());
		} else {
			filteredParallelPileup.setPileupsB(filteredPileupsB);
		}
	
		return filteredParallelPileup;
	}

	public boolean filter(final ParallelPileup parallelPileup) {
		final int[] variantBaseIs = getVariantBaseIs(parallelPileup);

		for (int variantBaseI : variantBaseIs) {
			if (filter(variantBaseI, parallelPileup)) {
				return true;
			}
		}

		return false;
	}
	
	public abstract boolean filter(final int variantBaseI, final ParallelPileup parallelPileup);

}