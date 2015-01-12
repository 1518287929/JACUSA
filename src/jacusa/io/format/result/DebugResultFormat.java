package jacusa.io.format.result;

import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.process.phred2prob.Phred2Prob;

// CHANGED
public class DebugResultFormat extends AbstractResultFormat {

	public static final char CHAR = 'X';
	
	public static final char COMMENT= '#';
	public static final char EMPTY 	= '*';
	public static final char SEP 	= '\t';
	public static final char SEP2 	= ',';

	private BaseConfig baseConfig;
	public Phred2Prob phred2Prob;

	public DebugResultFormat(BaseConfig baseConfig) {
		super(CHAR, "Debug BED like output");
		this.baseConfig = baseConfig;

		phred2Prob = Phred2Prob.getInstance(this.baseConfig.getBases().length);
	}

	@Override
	public String getHeader(ParallelPileup parallelPileup) {
		final StringBuilder sb = new StringBuilder();

		sb.append(COMMENT);

		// position (0-based)
		sb.append("contig");
		sb.append(getSEP());
		sb.append("start");
		sb.append(getSEP());
		sb.append("end");
		sb.append(getSEP());

		sb.append("name");
		sb.append(getSEP());

		// stat	
		sb.append("stat");
		sb.append(getSEP());
		
		sb.append("strand");
		sb.append(getSEP());
		
		// (1) first sample  infos
		addSampleHeader(sb, 'A', parallelPileup.getN1());
		sb.append(getSEP());
		// (2) second sample  infos
		addSampleHeader(sb, 'B', parallelPileup.getN2());
		
		return sb.toString();
	}
	
	private void addSampleHeader(StringBuilder sb, char sample, int replicates) {
		sb.append("bases");
		sb.append(sample);
		sb.append(1);
		if (replicates == 1) {
			return;
		}
		
		for (int i = 2; i <= replicates; ++i) {
			sb.append(SEP);
			sb.append("bases");
			sb.append(sample);
			sb.append(i);
		}
	}

	private StringBuilder convert2StringHelper(final ParallelPileup parallelPileup, final double value) {
		final StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition() - 1);
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());
		
		sb.append(SEP);
		sb.append("variant");
		
		sb.append(SEP);
		if (Double.isNaN(value)) {
			sb.append("NA");
		} else {
			sb.append(value);
		}

		sb.append(SEP);
		sb.append(parallelPileup.getStrand().character());

		// (1) first pileups
		addPileups(sb, parallelPileup.getPileups1());
		// (2) second pileups
		addPileups(sb, parallelPileup.getPileups2());
		
		return sb;
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		final StringBuilder sb = convert2StringHelper(parallelPileup, Double.NaN);
		return sb.toString();		
	}
	
	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, String filterInfo) {
		final StringBuilder sb = convert2StringHelper(parallelPileup, value);
		sb.append(SEP);
		sb.append(filterInfo);
		
		return sb.toString();
	}
	
	/*
	 * Helper function
	 */
	private void addPileups(StringBuilder sb, Pileup[] pileups) {
		// output sample: Ax,Cx,Gx,Tx
		for (Pileup pileup : pileups) {
			sb.append(SEP);
			int baseI = 0;
			sb.append(pileup.getCounts().getBaseCount()[baseI]);
			baseI++;
			for (; baseI < pileup.getCounts().getBaseCount().length ; ++baseI) {
				sb.append(SEP2);
				sb.append(pileup.getCounts().getBaseCount()[baseI]);
			}
		}
	}

	/* old version where filtered counts where part of ParallelPileup
	private void addFilterCounts(StringBuilder sb, Pileup[] pileups, Counts[][] filterCounts) {
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int filterCountsI = 0; filterCountsI < filterCounts[pileupI].length; ++filterCountsI) {
				if (filterCounts[pileupI][filterCountsI] != null) {
					Counts counts = filterCounts[pileupI][filterCountsI];
					sb.append(SEP);
					int baseI = 0;
					sb.append(counts.getBaseCount()[baseI]);
					baseI++;
					for (; baseI < counts.getBaseCount().length ; ++baseI) {
						sb.append(SEP2);
						sb.append(counts.getBaseCount()[baseI]);
					}
				}
			}
		}
	}
	*/

	/**
	 * Last column holds the final value
	 */
	@Override
	public double extractValue(String line) {
		String[] cols = line.split(Character.toString(SEP));
		return Double.parseDouble(cols[4]);
	}

	public char getCOMMENT() {
		return COMMENT;
	}

	public char getEMPTY() {
		return EMPTY;
	}

	public char getSEP() {
		return SEP;
	}
	
	public char getSEP2() {
		return SEP2;
	}

	@Override
	public String getFilterInfo(String line) {
		return Character.toString(getEMPTY());
	}
	
}