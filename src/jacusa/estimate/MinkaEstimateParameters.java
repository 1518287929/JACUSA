package jacusa.estimate;

import java.util.Arrays;

import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.BayesAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.CombinedAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.WeirMoMAlphaInit;
import jacusa.util.MathUtil;

import org.apache.commons.math3.special.Gamma;

public class MinkaEstimateParameters {

	private AbstractAlphaInit alphaInit;

	// options for paremeters estimation
	private int maxIterations;
	private double epsilon; 

	private int iterations;
	
	public MinkaEstimateParameters() {
		alphaInit = new CombinedAlphaInit(new WeirMoMAlphaInit(), new BayesAlphaInit());
		maxIterations = 100;
		epsilon = 0.001;
	}

	public MinkaEstimateParameters(
			final AbstractAlphaInit initialAlpha, 
			final int maxIterations, 
			final double epsilon) {
		this.alphaInit = initialAlpha;

		this.maxIterations = maxIterations;
		this.epsilon = epsilon;
	}

	public int getIterations() {
		return iterations;
	}
	
	// estimate alpha and returns loglik
	public double maximizeLogLikelihood(int[] baseIs, double[] alphaOld, double coverages[], double[][] matrix) {
		// optim "bounds"
		iterations = 0;
		
		boolean converged = false;

		// final int baseN = baseConfig.getBases().length;
		final int baseN = alphaOld.length;
		
		// init alpha new
		double[] alphaNew = new double[baseN];
		Arrays.fill(alphaNew, 0.0);

		// container see Minka
		double[] gradient = new double[baseN];
		double[] Q = new double[baseN];
		double b;
		double z;
		// holds pre-computed value
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		// log-likelihood
		double loglikOld = Double.NEGATIVE_INFINITY;
		double loglikNew = Double.NEGATIVE_INFINITY;

		int pileupN = coverages.length;

		// maximize
		while (iterations < maxIterations && ! converged) {
			// pre-compute
			summedAlphaOld = MathUtil.sum(alphaOld);
			digammaSummedAlphaOld = digamma(summedAlphaOld);
			trigammaSummedAlphaOld = trigamma(summedAlphaOld);

			// reset
			b = 0.0;
			double b_DenominatorSum = 0.0;
			for (int baseI : baseIs) {
				// reset
				gradient[baseI] = 0.0;
				Q[baseI] = 0.0;

				// System.out.println("baseI: " + baseI);
				for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(coverages[pileupI] + summedAlphaOld);
					// 
					gradient[baseI] += digamma(matrix[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(matrix[pileupI][baseI] + alphaOld[baseI]);
					Q[baseI] -= trigamma(alphaOld[baseI]);
				}

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = 0.0;
			for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
				z += trigammaSummedAlphaOld;
				z -= trigamma(coverages[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);

			loglikOld = getLogLikelihood(alphaOld, baseIs, coverages, matrix);
			
			// try update alphaNew
			boolean admissible = true; 		
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];

				if (alphaNew[baseI] < 0.0) {
					System.err.println("Reset! " + iterations);
					admissible = false;
				}
			}
			// check if alpha negative
			if (! admissible) {
				// try newton backtracking
				alphaNew = backtracking(alphaOld, baseIs, gradient, b, Q);
				if (alphaNew == null) {
					alphaNew = new double[baseN];

					// if backtracking did not work use Ronning1989 -> min_k X_ik 
					for (int baseI : baseIs) {		
						double min = Double.MAX_VALUE;
						for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
							min = Math.min(min, matrix[pileupI][baseI] / coverages[pileupI]);
						}
						alphaNew[baseI] = min;
					}
				}
			}

			// calculate log-likelihood for new alpha(s)
			loglikNew = getLogLikelihood(alphaNew, baseIs, coverages, matrix);

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, alphaNew.length);
			iterations++;

			/*
			System.out.print("iter: " + iterations + "\t=>\t");
			System.out.println(printAlpha(alphaNew));
			*/
		}

		return loglikNew;
	}

	private  double[] backtracking(
			final double[] alpha, 
			final int[] baseIs, 
			final double[] gradient, 
			final double b, 
			final double[] Q) {
		double[] alphaNew = new double[alpha.length];

		// try smaller newton steps
		double lamba = 1.0;
		// decrease by
		double offset = 0.01;

		while (lamba >= 0.0) {
			lamba = lamba - offset;

			boolean admissible = true;
			// adjust alpha with smaller newton step
			for (int baseI : baseIs) {
				alphaNew[baseI] = alpha[baseI] - lamba * (gradient[baseI] - b) / Q[baseI];
				// check if admissible
				if (alphaNew[baseI] < 0.0) {
					admissible = false;
					break;
				}
			}

			if (admissible) {
				return alphaNew;
			}
		}

		// could not find alpha(s)
		return null;
	}
	
	// calculate likelihood
	protected double getLogLikelihood(
			final double[] alpha, 
			final int[] baseIs, 
			final double coverages[], 
			final double[][] pileupMatrix) {
		double logLikelihood = 0.0;
		double alphaSum = MathUtil.sum(alpha);

		for (int pileupI = 0; pileupI < coverages.length; pileupI++) {
			logLikelihood += Gamma.logGamma(alphaSum);
			logLikelihood -= Gamma.logGamma(coverages[pileupI] + alphaSum);

			for (int baseI : baseIs) {
				logLikelihood += Gamma.logGamma(pileupMatrix[pileupI][baseI] + alpha[baseI]);
				logLikelihood -= Gamma.logGamma(alpha[baseI]);
			}
		}
		return logLikelihood;
	}

	protected double digamma(double x) {
		return Gamma.digamma(x);
	}

	protected double trigamma(double x) {
		return Gamma.trigamma(x);
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public AbstractAlphaInit getAlphaInit() {
		return alphaInit;
	}
	
	public void setAlphaInit(AbstractAlphaInit alphaInit) {
		this.alphaInit = alphaInit;
	}
	
}