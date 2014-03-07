package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.output.TmpOutputWriter;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.iterator.ParallelPileupIterator;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractParallelPileupWorker extends Thread {

	protected AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher;

	protected SAMFileReader[] readers1;
	protected SAMFileReader[] readers2;

	protected final Parameters parameters;
	protected ParallelPileupIterator parallelPileupIterator;

	protected StatisticCalculator statistic;

	protected final int threadId;
	protected int nextThreadId;
	
	protected int comparisons;

	// output related
	// current writer
	protected TmpOutputWriter tmpOutputWriter;
	// final result format
	protected AbstractResultFormat resultFormat;

	// indicates if computation is finished
	private boolean isFinished;

	public AbstractParallelPileupWorker(AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher, final AnnotatedCoordinate coordinate, final Parameters parameters) {
		this.parallelPileupWorkerDispatcher 	= parallelPileupWorkerDispatcher; 

		readers1				= parallelPileupWorkerDispatcher.createBAMFileReaders1();
		readers2				= parallelPileupWorkerDispatcher.createBAMFileReaders2();

		this.parameters 		= parameters;
		resultFormat 			= parameters.getResultFormat();

		isFinished 				= false;

		synchronized (parallelPileupWorkerDispatcher.getThreadContainer()) {
			threadId			= parallelPileupWorkerDispatcher.getThreadContainer().size();
		}
		nextThreadId			= -1;
		buildParallelPileupIterator(coordinate, parameters);

		final String tmpFilename = parameters.getOutput().getInfo() + "_tmp" + String.valueOf(threadId) + ".gz";
		try {
			tmpOutputWriter		= new TmpOutputWriter(tmpFilename);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		comparisons 			= 0;
	}

	public final void run() {
		processParallelPileupIterator(parallelPileupIterator);
		while(!isFinished) {
			AnnotatedCoordinate annotatedCoordinate = null;
			synchronized (parallelPileupWorkerDispatcher.getCoordinateProvider()) {
				if(parallelPileupWorkerDispatcher.getCoordinateProvider().hasNext()) {
					annotatedCoordinate = parallelPileupWorkerDispatcher.getCoordinateProvider().next();
				} else {
					isFinished = true;
				}
			}

			if(annotatedCoordinate != null) {
				buildParallelPileupIterator(annotatedCoordinate, parameters);
				processParallelPileupIterator(parallelPileupIterator);
			}

			synchronized (parallelPileupWorkerDispatcher) {
				parallelPileupWorkerDispatcher.notify();
			}
		}
		close();
	}

	public synchronized int getNextThreadId() {
		return nextThreadId;
	}

	public synchronized void setNextThreadId(int id) {
		nextThreadId = id;
	}

	final public int getThreadId() {
		return threadId;
	}

	protected void close() {
		close(readers1);
		close(readers2);
	}

	private void close(SAMFileReader[] readers) {
		for(SAMFileReader reader : readers) {
			if(reader != null) {
				reader.close();
			}
		}

		try {
			tmpOutputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param parallelPileupIterator
	 */
	abstract protected void processParallelPileupIterator(ParallelPileupIterator parallelPileupIterator);

	/**
	 * 
	 * @param coordinate
	 * @param parameters
	 * @return
	 */
	abstract protected ParallelPileupIterator buildParallelPileupIterator_Helper(AnnotatedCoordinate coordinate, Parameters parameters);

	/**
	 * 
	 * @param annotatedCoordinate
	 * @param parameters
	 */
	final private void buildParallelPileupIterator(AnnotatedCoordinate annotatedCoordinate, Parameters parameters) {
		parallelPileupWorkerDispatcher.processThreadId(this);

		// let implementing class build the iterator
		parallelPileupIterator = buildParallelPileupIterator_Helper(annotatedCoordinate, parameters);
	}

	public final int getComparisons() {
		return comparisons;
	}

	public final boolean isFinished() {
		return isFinished;
	}

	public final TmpOutputWriter getTmpOutputWriter() {
		return tmpOutputWriter;
	}

}
