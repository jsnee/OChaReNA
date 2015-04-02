package com.jophus.houghtransformation;


/**
 * Represents one calculation step in the hough transformation process.
 * 
 * @author SUN2K
 */
abstract class HTAbstractProcessStep {
	
	/**
	 * original image used as source for processing step.
	 */
	protected HTImage sourceImage;
	
	/**
	 * reference to global process data pool.
	 */
	protected HTProcessData processData;
	
	/**
	 * Contructor to create a new hough process step.
	 * @param sourceImage image that is used as base to calculate the new image.
	 * @param htProcessData reference to global process data pool
	 */
	public HTAbstractProcessStep(HTImage sourceImage, HTProcessData htProcessData) {
		if (sourceImage == null)
		{
			throw new HTException("Source image is invalid.");
		}
		if (htProcessData == null)
		{
			throw new HTException("ProcessData is invalid.");
		}
		this.sourceImage = sourceImage;
		this.processData = htProcessData;
	}
	
	/**
	 * Method that actually calculates the new image.
	 */
	public abstract void execute();
	
	/**
	 * Get the image calculated in this process step.
	 * @return
	 */
	public abstract HTImage getImage();
}
