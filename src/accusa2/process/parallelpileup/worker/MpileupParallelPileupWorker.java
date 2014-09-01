package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import accusa2.ACCUSA2;
import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.pileup.iterator.UnstrandedParallelPileupWindowIterator;
import accusa2.process.parallelpileup.dispatcher.MpileupParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public class MpileupParallelPileupWorker extends AbstractParallelPileupWorker {

	public MpileupParallelPileupWorker(MpileupParallelPileupWorkerDispatcher workerDispatcher, Parameters parameters) {
		super(workerDispatcher, parameters);
	}

	@Override
	protected void processParallelPileupIterator(AbstractParallelPileupWindowIterator parallelPileupIterator) {
		ACCUSA2.printLog("Started screening contig " + 
				parallelPileupIterator.getAnnotatedCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getAnnotatedCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getAnnotatedCoordinate().getEnd());

		while (parallelPileupIterator.hasNext()) {
			// considered comparisons
			comparisons++;

			StringBuilder sb = new StringBuilder();
			ParallelPileup parallelPileup = parallelPileupIterator.next();
			sb.append(resultFormat.convert2String(parallelPileup, 0));
			try {
				tmpOutputWriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected AbstractParallelPileupWindowIterator buildParallelPileupIterator(AnnotatedCoordinate coordinate, Parameters parameters) {
		return new UnstrandedParallelPileupWindowIterator(coordinate, readersA, readersB, parameters);
	}

}