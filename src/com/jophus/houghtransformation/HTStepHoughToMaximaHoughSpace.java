package com.jophus.houghtransformation;

/**
 * Hough transformation process step to threshold the houg space image.
 * 
 * @author SUN2K
 *
 */
class HTStepHoughToMaximaHoughSpace extends HTAbstractProcessStep {

	/**
	 * Image of step: Hough space - hough space local maxima image.
	 */
	private HTGrayImage maximaHoughSpaceImage;
	
	/**
	 * Constructor.
	 * @param originalImage image used as source to calculate new one.
	 * @param htProcessData reference to global process data.
	 */
	public HTStepHoughToMaximaHoughSpace(HTImage originalImage,
			HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		maximaHoughSpaceImage = null;
	}

	@Override
	public void execute() {
		if (maximaHoughSpaceImage == null)
		{
			maximaHoughSpaceImage = new HTGrayImage(sourceImage);
		}
		
		// find global maximum
		int globalMax = 0;
		for (int angle = 0; angle < processData.getAngleDim(); angle++)
		{
			for (int r = 0; r < processData.getRDimTop(); r++)
			{
				if (processData.getHoughspaceTopOrigin(angle,r) > globalMax)
				{
					globalMax = processData.getHoughspaceTopOrigin(angle,r);
				}
			}
		}
		
		// find relative threshold
		int houghLineThreshold =  (int)((double)(processData.getHoughRelativeMaxima() / (double)100) * globalMax);
		processData.setHoughLineThreshold(houghLineThreshold);
		
		for (int angle = 0; angle < processData.getAngleDim(); angle++)
		{
			for (int r = 0; r < processData.getRDimTop(); r++)
	        {
				if (processData.getHoughspaceTopOrigin(angle,r) > houghLineThreshold)
				{
					maximaHoughSpaceImage.setPixel(angle, r, 255);
				}
				else
				{
					maximaHoughSpaceImage.setPixel(angle, r, 0);
				}
	        }
		}
	}

	@Override
	public HTImage getImage() {
		return maximaHoughSpaceImage;
	}

}
