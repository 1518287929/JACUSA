package jacusa.method.call.statistic.dirmult;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.MinkaEstimateParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.BayesAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.CombinedAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.ConstantAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.MeanAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.RonningAlphaInit;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;

import java.text.DecimalFormat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public abstract class AbstractDirMultStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;

	protected boolean onlyObservedBases;
	protected boolean showAlpha;

	protected double[] alpha1;
	protected double[] alpha2;
	protected double[] alphaP;

	protected MinkaEstimateParameters estimateAlpha;

	public AbstractDirMultStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters 	= parameters;
		final int n 		= baseConfig.getBaseLength();
		this.baseConfig 	= baseConfig;
		phred2Prob 			= Phred2Prob.getInstance(n);
		onlyObservedBases 	= false;
		showAlpha			= false;

		estimateAlpha		= new MinkaEstimateParameters();
	}

	protected abstract void populate(final Pileup[] pileups, final int[] baseIs, double[] pileupCoverages, double[][] pileupMatrix);

	@Override
	public synchronized void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		result.setStatistic(statistic);

		if (showAlpha) {
			DecimalFormat df = new DecimalFormat("0.00"); 
			StringBuilder sb = new StringBuilder();
			sb.append("alpha1=");
			sb.append(df.format(alpha1[0]));
			for (int i = 1; i < alpha1.length; ++i) {
				sb.append("|");
				sb.append(df.format(alpha1[i]));
			}
			sb.append(";alpha2=");
			sb.append(df.format(alpha2[0]));
			for (int i = 1; i < alpha2.length; ++i) {
				sb.append("|");
				sb.append(df.format(alpha2[i]));
			}
			sb.append(";alphaP=");
			sb.append(df.format(alphaP[0]));
			for (int i = 1; i < alphaP.length; ++i) {
				sb.append("|");
				sb.append(df.format(alphaP[i]));
			}
			result.addInfo(sb.toString());
		}
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = getBaseIs(parallelPileup);
		int baseN = baseConfig.getBaseLength();

		ChiSquareDist dist = new ChiSquareDist(baseN);

		alpha1 = new double[baseN];
		double[] pileupCoverages1 = new double[parallelPileup.getN1()];
		double[][] pileupMatrix1 = new double[parallelPileup.getN1()][baseN];

		alpha2 = new double[baseN];
		double[] pileupCoverages2 = new double[parallelPileup.getN2()];
		double[][] pileupMatrix2 = new double[parallelPileup.getN2()][baseN];

		alphaP = new double[baseN];
		double[] pileupCoveragesP = new double[parallelPileup.getN()];
		double[][] pileupMatrixP = new double[parallelPileup.getN()][baseN];

		populate(parallelPileup.getPileups1(), baseIs, pileupCoverages1, pileupMatrix1);
		populate(parallelPileup.getPileups2(), baseIs, pileupCoverages2, pileupMatrix2);
		populate(parallelPileup.getPileupsP(), baseIs, pileupCoveragesP, pileupMatrixP);

		double p = -1.0;
		try {
			// estimate alphas
			double logLikelihood1 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha1, pileupCoverages1, pileupMatrix1);
			double logLikelihood2 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha2, pileupCoverages2, pileupMatrix2);
			double logLikelihoodP = estimateAlpha.maximizeLogLikelihood(baseIs, alphaP, pileupCoveragesP, pileupMatrixP);

			// LRT
			double z = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));
			p = 1 - dist.cdf(z);
		} catch (StackOverflowError e) {
			System.out.println("Warning: Numerical Stability");
			System.out.println(parallelPileup.getContig());
			System.out.println(parallelPileup.getStart());
			System.out.println(parallelPileup.prettyPrint());

			// TODO try to change init of alpha
			return Double.MAX_VALUE;
		}

		return p;
	}

	// Debug function
	protected void printAlpha(double[] alphas) {
		StringBuilder sb = new StringBuilder();
		for (double alpha : alphas) {
			sb.append(Double.toString(alpha));
			sb.append("\t");
		}
		System.out.println(sb.toString());
	}

	@Override
	public boolean filter(double value) {
		return parameters.getThreshold() < value;
	}

	// format -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:onlyObserved
	@Override
	public boolean processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		boolean r = false;

		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("epsilon")) {
				estimateAlpha.setEpsilon(Double.parseDouble(value));
				r = true;
			} else if(key.equals("maxIterations")) {
				estimateAlpha.setMaxIterations(Integer.parseInt(value));
				r = true;
			} else if(key.equals("onlyObserved")) {
				onlyObservedBases = true;
				r = true;
			} else if(key.equals("showAlpha")) {
				showAlpha = true;
				r = true;
			} else if(key.equals("initAlpha")) {
				// ugly
				String initAlphaClass = value.split(Character.toString(','))[0];
				AbstractAlphaInit alphaInit = null;
				if (initAlphaClass.equals("bayes")) {
					alphaInit = new BayesAlphaInit();
				} else if (initAlphaClass.equals("combined")) {
					alphaInit = new CombinedAlphaInit();
				} else if (initAlphaClass.equals("constant")) {
					double constant = -1d;
					for (String v : value.split(Character.toString(','))) {
						String[] kv2 = v.split("=");
						String key2 = kv[0];
						String value2 = new String();
						if (kv2.length == 2) {
							value2 = kv2[1];
						}
						if (key2.equals("value")) {
							constant = Double.parseDouble(value2);
						}
					}
					if (constant == -1d) {
						throw new IllegalArgumentException(line + "\nConstant has to be > 0");
					}
					alphaInit = new ConstantAlphaInit(constant);
				} else if (initAlphaClass.equals("mean")) {
					alphaInit = new MeanAlphaInit();
				} else if (initAlphaClass.equals("Ronning")) {
					alphaInit = new RonningAlphaInit();
				}

				estimateAlpha.setAlphaInit(alphaInit);
				
			}
		}

		return r;
	}

	public int[] getBaseIs(ParallelPileup parallelPileup) {
		if (onlyObservedBases) {
			return parallelPileup.getPooledPileup().getAlleles();
		}

		return baseConfig.getBasesI();
	}

}