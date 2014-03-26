package accusa2.io.format;

import net.sf.samtools.SAMUtils;
import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.Pileup.STRAND;
import accusa2.process.phred2prob.Phred2Prob;

public class PileupResultFormat extends PileupFormat {

	private Parameters parameters;

	public PileupResultFormat(Parameters paramters) {
		super('A', "pileup like ACCUSA result format");
		this.parameters = paramters;
	}

	public String getHeader() {
		final StringBuilder sb = new StringBuilder();
		sb.append("#");

		sb.append("contig");
		sb.append(getSEP());

		sb.append("position");
		sb.append(getSEP());

		// (1) first sample infos
		sb.append("strand1");
		sb.append(getSEP());
		sb.append("bases1");
		sb.append(getSEP());
		sb.append("quals1");
		
		sb.append(getSEP());
		
		// (2) second sample infos
		sb.append("strand2");
		sb.append(getSEP());
		sb.append("bases2");
		sb.append(getSEP());
		sb.append("quals2");

		sb.append(getSEP());

		sb.append("unfiltered");

		for(final AbstractFilterFactory abstractPileupFilterFactory : parameters.getPileupBuilderFilters().getFilterFactories()) {
			sb.append(getSEP());
			sb.append("filtered_");
			sb.append(abstractPileupFilterFactory.getC());
		}

		if(parameters.getPileupBuilderFilters().hasFiters()) {
			sb.append(getSEP());
			sb.append("filtered");
		}

		sb.append(getSEP());
		sb.append("fdr");
		return sb.toString();
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		StringBuilder sb = new StringBuilder(convert2String(parallelPileup));
		// add unfiltered value
		sb.append(SEP);
		sb.append(value);
		return sb.toString();
	}
	
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(SEP));
		return Double.parseDouble(cols[cols.length - 1]);
	}
	
	@Override // FIXME replicates
	public ParallelPileup extractParallelPileup(String line) {
		final int offset = 3;

		// holds a line as an array 
		String[] cols = line.split(Character.toString(SEP));

		// coordinates
		String contig = cols[0];
		int position = Integer.parseInt(cols[1]);

		// (1) first sample infos
		STRAND strand1 = Pileup.STRAND.getEnum(cols[2]);
		// number of replicates
		int n1 = parameters.getPathnames1().length;
		String[] bases1 = new String[n1];
		String[] quals1 = new String[n1];
		for(int i = 0; i < n1; ++i) {
			bases1[i] = cols[offset + 2 * i];
			quals1[i] = cols[offset + 2 * i + 1];
		}

		// (2) second sample infos
		STRAND strand2 = Pileup.STRAND.getEnum(cols[7]);
		// number of replicates
		int n2 = parameters.getPathnames2().length; 
		String[] bases2 = new String[n2];
		String[] quals2 = new String[n1];
		for(int i = 0; i < n2; ++i) {
			bases2[i] = cols[2 * n1 + 1 + offset + 2 * i];
			quals2[i] = cols[2 * n1 + 1 + offset + 2 * i + 1];
		}

		// container
		ParallelPileup parallelPileup = new ParallelPileup(n1, n2);
		// set first sample(s)
		parallelPileup.setPileups1(extractPileups(contig, position, n1, strand1, bases1, quals1));
		// set second sample(s)
		parallelPileup.setPileups2(extractPileups(contig, position, n2, strand2, bases2, quals2));
		
		return parallelPileup;
	}
	
	/**
	 * Helper function
	 * @param contig
	 * @param position
	 * @param replicates
	 * @param strand
	 * @param bases
	 * @return
	 */
	// FIXME
	private Pileup[] extractPileups(String contig, int position, int replicates, STRAND strand, String[] bases, String[] quals) {
		Pileup[] pileups = new Pileup[replicates];
		// create pileups
		for(int i = 0; i < replicates; ++i) {
			Pileup pileup = new Pileup(contig, position, strand);
			pileups[i] = pileup;
		}

		for(int i = 0; i < replicates; ++i) {
			char[] basesA = bases[i].toCharArray();
			byte[] qualsA = SAMUtils.fastqToPhred(quals[i]);

			for(int j = 0; j < basesA.length; ++j) {
				int base = Pileup.BASE2INT.get(basesA[j]);
				pileups[i].addBase(base, qualsA[j]); 
			}
		}

		return pileups;
	}

	@Override
	protected void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		sb.append(SEP);
		sb.append(strand.character());
		
		for(Pileup pileup : pileups) {

			sb.append(SEP);
			
			for(int base : pileup.getAlleles()) {
				
				// print bases 
				for(int i = 0; i < pileup.getBaseCount()[base]; ++i) {
					sb.append(Pileup.BASES2[base]);
				}
			}

			sb.append(SEP);

			// print quals
			for(int base : pileup.getAlleles()) {
				for(byte qual = 0; qual < Phred2Prob.MAX_Q; ++qual) {

					int count = pileup.getQualCount(base, qual);
					if(count > 0) {
						// repeat count times
						for(int j = 0; j < count; ++j) {
							sb.append(SAMUtils.phredToFastq(qual));
						}
					}
				}
			}
		}
	}
	
}
