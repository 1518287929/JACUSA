package jacusa.cli.parameters;

public class TwoSamplePileupParameters extends AbstractParameters implements hasSample2 {

	private SampleParameters sampleB;

	public TwoSamplePileupParameters() {
		super();

		sampleB	= new SampleParameters();
	}

	@Override
	public SampleParameters getSample2() {
		return sampleB;
	}


}