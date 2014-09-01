package accusa2.method.statistic;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

/**
 * 
 * @author michael
 */

public final class MixtureDirichletStatistic implements StatisticCalculator {

	protected final Parameters parameters; 
	
	protected final Phred2Prob phred2Prob;

	protected ChiSquareDist dist = new ChiSquareDist(6);
	
	public MixtureDirichletStatistic(Parameters parameters) {
		this.parameters 	= parameters;
		
		phred2Prob 			= Phred2Prob.getInstance(parameters.getBaseConfig().getBases().length);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new MixtureDirichletStatistic(parameters);
	}

	protected double getDensity(final int[] bases, final Pileup[] pileups) {
		double density = 0.0;
		
		final int pileupN = pileups.length;
		// weights by coverage
		final double[] weights = new double[pileupN] ;
		int totalCoverage = 0;
		// prob. vector per pileup
		final double[][] pileupProbVectors = new double[pileupN][bases.length];
		// alpha
		final double alphas[][] = new double[pileupN][bases.length];
		final double alpha[] = new double[bases.length];
		Arrays.fill(alpha, 0.0);
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			// calculate weights
			final Pileup pileup = pileups[pileupI]; // has to be
			int coverage = pileup.getCoverage();
			weights[pileupI] = (double)coverage;
			totalCoverage += coverage;
			
			// calculate prob. vectors
			double[] probVector = phred2Prob.colSum(bases, pileup);
			pileupProbVectors[pileupI] = probVector;
			
			for (int baseI = 0; baseI < bases.length; ++baseI) {
				alphas[pileupI][baseI] = probVector[baseI] * coverage;
			}
		}
		
		// calculate alphas need to be divided by total coverage
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI = 0; baseI < bases.length; ++baseI) {
				alpha[baseI] += alphas[pileupI][baseI] * weights[pileupI] / totalCoverage;
			}
		}
		
		// calculate density
		DirichletDist dirichlet = new DirichletDist(alpha);
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			double[] probVector = pileupProbVectors[pileupI]; 
			density += Math.log(Math.max(Double.MIN_VALUE, dirichlet.density(probVector)));
		}

		return density;
	}
	
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int bases[] = {0, 1, 2, 3};
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample(s)
		double density1 = getDensity(bases, parallelPileup.getPileupsA());
		
		// second sample(s)
		double density2 = getDensity(bases, parallelPileup.getPileupsB());
		
		// pooled sample(s)
		final Pileup[] pileupsP = parallelPileup.getPileupsP();
		double densityP = getDensity(bases, pileupsP);

		final double z = -2 * (densityP) + 2 * (density1 + density2);

		// only positive values are allowed
		if(z < 0.0 ) {
			return 1.0;
		}
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	@Override
	public String getDescription() {
		return "Mixture of Dirichlets";
	}

	@Override
	public String getName() {
		return "mix";
	}

}