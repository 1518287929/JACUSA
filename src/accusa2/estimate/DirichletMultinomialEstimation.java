package accusa2.estimate;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class DirichletMultinomialEstimation extends AbstractEstimateParameters {

	// private final double digamma = Math.log(0.5);
	private final int maxIterations = 100;
	private final double epsilon = 1.0/(double)(10^6); 

	public DirichletMultinomialEstimation(Phred2Prob phred2Prob) {
		super("DirMult", "Dirichlet-Multinomial distribution", phred2Prob);
	}

	@Override
	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups) {
		// parameters
		int iteration = 0;
		boolean converged = false;

		// TODO make better estimation
		// actual values
		double[] alphaOld = new double[baseIs.length];
		Arrays.fill(alphaOld, 0.0);
		for (Pileup pileup : pileups) {
			double[] sum = phred2Prob.colSum(baseIs, pileup);
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaOld[baseI] += sum[baseI];
			}
		}
		for (int baseI = 0; baseI < baseIs.length; ++baseI) {
			alphaOld[baseI] /= (double)pileups.length;
		}

		double[] alphaNew = new double[baseIs.length];
		Arrays.fill(alphaNew, 0.0);

		// container 
		double[] gradient = new double[baseIs.length];
		double[] Q = new double[baseIs.length];
		double b;
		double z;
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;

		// pileup related counts/containters/with prior knowledge
		double[][] nIK = new double[pileups.length][baseIs.length];
		double[] nI = new double[pileups.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			nI[pileupI] = (double)pileups[pileupI].getCoverage();
			nIK[pileupI] = phred2Prob.colSum(baseIs, pileups[pileupI]);
		}

		// maximize
		while (iteration < maxIterations && ! converged) {
			// pre-compute
			summedAlphaOld = sum(alphaOld);
			digammaSummedAlphaOld = digamma(summedAlphaOld);
			trigammaSummedAlphaOld = trigamma(summedAlphaOld);

			// reset
			b = 0.0;
			double b_DenominatorSum = 0.0;
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				// reset
				gradient[baseI] = 0.0;
				Q[baseI] = 0.0;

				for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(nI[pileupI] + summedAlphaOld);

					gradient[baseI] += digamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					Q[baseI] -= trigamma(alphaOld[baseI]);
				}

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = 0.0;
			for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
				z += trigammaSummedAlphaOld;
				z -= trigamma(nI[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);
			
			double loglikOld = getLogLikelihood(alphaOld, baseIs, pileups);
			// update alphaNew
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				// check that alpha is not < 0
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005; // hard set
				}
			}
			double loglikNew = getLogLikelihood(alphaNew, baseIs, pileups);
			// update value
			alphaOld = alphaNew.clone();

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			iteration++;
		}

		return alphaNew;
	}

	@Override
	public double[] estimateExpectedValue(int[] baseIs, Pileup[] pileups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] estimateProbs(int[] baseIs, Pileup[] pileups) {
		// TODO Auto-generated method stub
		return null;
	}

	protected double digamma(double x) {
		return Gamma.digamma(x);
	}

	protected double trigamma(double x) {
		return Gamma.trigamma(x);
	}

	public double sum(double[] values) {
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		return sum;
	}

	// calculate likelihood
	public double getLogLikelihood(double[] alpha, int[] baseIs, Pileup[] pileups) {
		double logLikelihood = 0.0;
		double alphaSum = sum(alpha);

		for (Pileup pileup : pileups) {
			double nI = (double)pileup.getCoverage() ;
			double[] nIK = phred2Prob.colSum(baseIs, pileup);

			logLikelihood += Gamma.logGamma(alphaSum);
			logLikelihood -= Gamma.logGamma(nI + alphaSum);

			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				logLikelihood += Gamma.logGamma(nIK[baseI] + alpha[baseI]);
				logLikelihood -= Gamma.logGamma(alpha[baseI]);
			}
		}
		return logLikelihood;
	}

}