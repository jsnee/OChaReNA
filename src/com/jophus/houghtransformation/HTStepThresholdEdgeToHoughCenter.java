package com.jophus.houghtransformation;

/**
 * Hough transformation process step of visualize the hough space.
 * Origin: at center of the image.
 * X-Axis: r (distance)
 * Y-Axis: angle
 * @author SUN2K
 *
 */
public class HTStepThresholdEdgeToHoughCenter extends HTAbstractProcessStep {

	/**
	 * Image of step: treshold edge image -> Hough space image with centered origin.
	 */
	private HTGrayImage houghSpaceImageCenterOrigin;
	
	/**
	 * Constructor.
	 * @param originalImage image used as source to calculate new one.
	 * @param htProcessData reference to global process data.
	 */
	public HTStepThresholdEdgeToHoughCenter(HTImage originalImage,
			HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		houghSpaceImageCenterOrigin = null;
	}

	@Override
	public void execute() {
		if (houghSpaceImageCenterOrigin == null)
		{
			// build a new image with width = angle and height = r
			houghSpaceImageCenterOrigin = new HTGrayImage(processData.getAngleDim(),
				processData.getRDimCenter());
		}
		
		for(int x = 0; x < houghSpaceImageCenterOrigin.getWidth(); x ++)
		{
			for(int y = 0; y < houghSpaceImageCenterOrigin.getHeight(); y++)  
	        {
				houghSpaceImageCenterOrigin.setPixel(x, y, 
						HTUtil.clipByte(processData.getHoughspaceCenterOrigin(x, y) * 
								processData.getHoughSpaceBrightFactor()) );
	        }
		}
	}

	@Override
	public HTImage getImage() {
		return houghSpaceImageCenterOrigin;
	}

}
