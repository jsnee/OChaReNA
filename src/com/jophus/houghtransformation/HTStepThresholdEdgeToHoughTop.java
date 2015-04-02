package com.jophus.houghtransformation;

/**
 * Hough transformation process step of visualize the hough space.
 * Origin: at top midpoint of the image.
 * X-Axis: r (distance)
 * Y-Axis: angle
 * @author SUN2K
 *
 */
public class HTStepThresholdEdgeToHoughTop extends HTAbstractProcessStep{

	/**
	 * Image of step: treshold edge image -> Hough space image with top left origin.
	 */
	private HTGrayImage houghSpaceImageTopOrigin;
	
	/**
	 * Constructor.
	 * @param originalImage image used as source to calculate new one.
	 * @param htProcessData reference to global process data.
	 */
	public HTStepThresholdEdgeToHoughTop(HTImage originalImage,
			HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		houghSpaceImageTopOrigin = null;
	}

	@Override
	public void execute() {
		if (houghSpaceImageTopOrigin == null)
		{
			// build a new image with width = angle and height = r
			houghSpaceImageTopOrigin = new HTGrayImage(processData.getAngleDim(), 
				processData.getRDimTop());
		}
		
		for(int x = 0; x < houghSpaceImageTopOrigin.getWidth(); x ++)
		{
			for(int y = 0; y < houghSpaceImageTopOrigin.getHeight(); y++)  
	        {
				houghSpaceImageTopOrigin.setPixel(x, y, 
						HTUtil.clipByte(processData.getHoughspaceTopOrigin(x, y) * 
								processData.getHoughSpaceBrightFactor()) );
	        }
		}
	}

	@Override
	public HTImage getImage() {
		return houghSpaceImageTopOrigin;
	}

}
