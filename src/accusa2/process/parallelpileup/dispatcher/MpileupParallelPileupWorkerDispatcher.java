package accusa2.process.parallelpileup.dispatcher;


import java.io.File;
import java.io.IOException;

import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.TmpResultFormat;
import accusa2.io.output.Output;
import accusa2.io.output.TmpOutputReader;
import accusa2.io.output.TmpOutputWriter;
import accusa2.process.parallelpileup.worker.MpileupParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class MpileupParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<MpileupParallelPileupWorker> {

	public MpileupParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);
	}

	@Override
	protected void processFinishedWorker(MpileupParallelPileupWorker processParallelPileup) {
		comparisons += processParallelPileup.getComparisons();
	}

	@Override
	protected MpileupParallelPileupWorker buildNextParallelPileupWorker() {
		return new MpileupParallelPileupWorker(this, parameters);
	}

	// FIXME redundant code see ACCUSA25_ParallelPileupWorkerDispatcher
	@Override
	protected void writeOuptut() {
		final Output output = parameters.getOutput();
		final AbstractResultFormat resultFormat = new TmpResultFormat(parameters.getResultFormat());

		// build reader array
		TmpOutputReader[] tmpOutputReaders = new TmpOutputReader[threadContainer.size()];
		for(int i = 0; i < threadContainer.size(); ++i) {
			final TmpOutputWriter tmpOutputWriter = threadContainer.get(i).getTmpOutputWriter();
			TmpOutputReader tmpOutputReader;
			try {
				tmpOutputReader = new TmpOutputReader(tmpOutputWriter.getInfo());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			tmpOutputReaders[i] = tmpOutputReader;
		}

		// read data and change readers based on meta info/nextThreadId on the fly to reconstruct order of output
		TmpOutputReader tmpOutputReader = tmpOutputReaders[0];
		try {
			String line = null;
			while((line = tmpOutputReader.readLine()) != null) {
				if(line.charAt(0) == resultFormat.getCOMMENT()) {
					int nextThreadId = Integer.parseInt(line.substring(1));
					tmpOutputReader = tmpOutputReaders[nextThreadId];
				} else {
					output.write(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for(int i = 0; i < threadContainer.size(); ++i) {
			try {
				tmpOutputReaders[i].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// leave tmp files if needed
			if(!parameters.getDebug()) {
				new File(threadContainer.get(i).getTmpOutputWriter().getInfo()).delete();
			}
		}
		
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
