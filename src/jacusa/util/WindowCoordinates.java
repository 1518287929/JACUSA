package jacusa.util;

public class WindowCoordinates {

	private String contig;
	private int genomicWindowStart;
	
	private int windowSize;
	private int maxGenomicPosition;

	public WindowCoordinates(final String contig, final int genomicWindowStart, final int windowSize, final int maxGenomicPosition) {
		this.contig = contig;
		this.genomicWindowStart = genomicWindowStart;
		this.windowSize = windowSize;
		this.maxGenomicPosition = maxGenomicPosition;
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
	}

	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}

	public void setGenomicWindowStart(int genomicWindowStart) {
		this.genomicWindowStart = genomicWindowStart;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getMaxGenomicPosition() {
		return maxGenomicPosition;
	}

	public void setMaxGenomicPosition(int maxGenomicPosition) {
		this.maxGenomicPosition = maxGenomicPosition;
	}
	
	/**
	 * End of window (inclusive)
	 * @return
	 */
	public int getWindowEnd() {
		return Math.min(genomicWindowStart + windowSize - 1, maxGenomicPosition);
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInGenome(int genomicPosition) {
		return genomicPosition <= maxGenomicPosition && genomicPosition > 0;
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInWindow(int genomicPosition) {
		return genomicPosition >= genomicWindowStart && genomicPosition <= getWindowEnd();
	}

	/**
	 * Calculates genomicPosition or -1 or -2 if genomicPosition is outside the window
	 * -1 if downstream of windowEnd
	 * -2 if upstream of windowStart
	 * @param genomicPosition
	 * @return
	 */
	public int convertGenomicPosition2WindowPosition(int genomicPosition) {
		if(genomicPosition < genomicWindowStart) {
			return -2;
		} else if(genomicPosition > getWindowEnd()){
			return -1;
		}

		return genomicPosition - genomicWindowStart;
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	public int getGenomicPosition(int windowPosition) {
		return genomicWindowStart + windowPosition;
	}

}