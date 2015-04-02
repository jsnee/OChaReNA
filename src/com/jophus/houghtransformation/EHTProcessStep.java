package com.jophus.houghtransformation;


/**
 * Defines the single steps in the HT process.
 * 
 * @author Sun2k
 */
public enum EHTProcessStep {

	STEP_ORIGINAL,
	STEP_GRAYSCALE,
	STEP_EDGE_DETECTION,
	STEP_EDGE_TRESHOLD,
	STEP_HOUGH_SPACE_TOP,
	STEP_HOUGH_SPACE_CENTER,
	STEP_HOUGH_SPACE_FILTERED,
	STEP_ORIGINAL_LINES_OVERLAYED;
	
	
	/**
	 * Get the successive HT step.
	 * @return
	 */
	public EHTProcessStep getNextStep()
	{
		if (this.ordinal() >= values().length)
			throw new IllegalStateException();
		return values()[(ordinal()+1) % values().length];
	}
	
	/**
	 * Check if this step is before the requested in the HT pipeline.
	 * 
	 * @param step the step compare to this step.
	 * @return true if this step is before the requested step, otherwise false.
	 * Note that if the step matches, false is returned.
	 */
	public boolean isLowerStep(EHTProcessStep step)
	{
		return (this.ordinal() < step.ordinal());
	}
}
