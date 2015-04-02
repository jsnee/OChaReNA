package com.jophus.houghtransformation;

/**
 * Hough transformation process step to apply a threshold to the edge image to remove noise
 * of detected edges. Result is a 2 color image.
 * 
 * @author SUN2K
 *
 */
class HTStepEdgeToThresholdEdge extends HTAbstractProcessStep {

	/**
	 * Image of step: edge detection -> edge detection with threshold applied.
	 */
	private HTGrayImage edgeTresholdImage;
	
	/**
	 * Constructor.
	 * @param originalImage image used as source to calculate new one.
	 * @param htProcessData reference to global process data.
	 */
	public HTStepEdgeToThresholdEdge(HTImage originalImage, HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		edgeTresholdImage = null;
	}

	/**
	 * Build a black-white threshold image from the edge detection image.
	 */
	@Override
	public void execute() {
		if (edgeTresholdImage == null)
		{
			edgeTresholdImage = new HTGrayImage(sourceImage);
		}
				
		for(int x = 0; x < edgeTresholdImage.getWidth(); x ++)
		{
			for(int y = 0; y < edgeTresholdImage.getHeight(); y++)  
	        {
				int pixel = edgeTresholdImage.getSourcePixel(x, y);
				
				if (pixel < processData.getEdgeTreshold())
				{
					pixel = 0;
				}
				else
				{
					pixel = 255;					
				}
				
				edgeTresholdImage.setPixel(x, y, pixel);
	        }
		}
	}

	@Override
	public HTImage getImage() {
		return edgeTresholdImage;
	}

}
