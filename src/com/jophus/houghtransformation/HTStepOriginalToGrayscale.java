package com.jophus.houghtransformation;


/**
 * Hough transformation process step to convert original image to grayscale image.
 *  
 * @author SUN2K
 *
 */
public class HTStepOriginalToGrayscale extends HTAbstractProcessStep {

	/**
	 * Image of step: the original image -> converted to grayscale.
	 */
	private HTGrayImage grayScaleImage;
	
	/**
	 * Constructor.
	 *  
	 * @param originalImage source image used for this step.
	 */
	public HTStepOriginalToGrayscale(HTImage originalImage, HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		grayScaleImage = null;
	}	
	
	@Override
	public void execute() {
		if (grayScaleImage == null)
		{
			grayScaleImage = new HTGrayImage((HTColorImage) sourceImage);
		}
	}
	
	public HTImage getImage()
	{
		return grayScaleImage;
	}

}
