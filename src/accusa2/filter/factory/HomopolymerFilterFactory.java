package accusa2.filter.factory;

import accusa2.filter.process.AbstractPileupBuilderFilter;
import accusa2.filter.process.HomopolymerParallelPileupFilter;
import accusa2.filter.process.HomopolymerPileupBuilderFilter;

public class HomopolymerFilterFactory extends AbstractFilterFactory {

	private int length;
	private int distance;
	//private Set<Character> bases;

	public HomopolymerFilterFactory() {
		// TODO more descriptive
		super('Y', "Filter homopolymers. Default: none");
	}

	@Override
	public HomopolymerParallelPileupFilter getParallelPileupFilterInstance() {
		return new HomopolymerParallelPileupFilter(getC(), getLength(), getDistance(), getParameters());
	}

	@Override
	public AbstractPileupBuilderFilter getPileupBuilderFilterInstance() {
		return new HomopolymerPileupBuilderFilter(getC(), length, distance);
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format Y:length:distance 
		for(int i = 1; i < s.length; ++i) {
			int value = Integer.valueOf(s[i]);

			switch(i) {
			case 1:
				setLength(value);
				break;

			case 2:
				setDistance(value);
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + length);
			}
		}
	}

	public final void setLength(int length) {
		this.length = length;
	}

	public final int getLength() {
		return length;
	}

	public final void setDistance(int distance) {
		this.distance = distance;
	}

	public final int getDistance() {
		return distance;
	}

}
