package com.jophus.houghtransformation;

import java.awt.Color;

/**
 * Hough transformation process step to threshold the houg space image.
 * 
 * @author SUN2K
 *
 */
class HTStepMaximaToOriginalOverlayedImage extends HTAbstractProcessStep {

	/**
	 * Image of step: Original image with de-houghed line data
	 */
	private HTColorImage originalOverlayedImage;
	
	/**
	 * Constructor.
	 * @param originalImage image used as source to calculate new one.
	 * @param htProcessData reference to global process data.
	 */
	public HTStepMaximaToOriginalOverlayedImage(HTImage originalImage,
			HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		originalOverlayedImage = null;
	}
	
	@Override
	public void execute() {
		if (originalOverlayedImage == null)
		{
			originalOverlayedImage = new HTColorImage(sourceImage);
		}
		
		double[] cosLookupTable = processData.getCosTable();
		double[] sinLookupTable = processData.getSinTable();
		
		int offset = processData.getRDimCenterOffset();
		
		int maxLength = (int)Math.ceil(Math.sqrt(originalOverlayedImage.getWidth() * originalOverlayedImage.getWidth() +
				originalOverlayedImage.getHeight() *  originalOverlayedImage.getHeight()));
		
		for (int angle = 0; angle < processData.getAngleDim(); angle++)
		{
			for (int r = 0; r < processData.getRDimCenter(); r++)
			{
				if (processData.getHoughspaceCenterOrigin(angle,r) > processData.getHoughLineThreshold())
				{
					/* get coordinates */
					int x = (int)((r-offset) * cosLookupTable[angle]);
					int y = (int)((r-offset) * sinLookupTable[angle]);
					/* mark the point on the line red */
					originalOverlayedImage.setPixel(x, y, Color.RED, 5);
					/* this would draw the position vector of the point on the line */
					//originalOverlayedImage.drawOverlayLine(0, 0, x, y, Color.GRAY);
					
					/* rotate the angle by 90 degress to get the angle of the detected line and
					 * also do the conversion to our coordinate system */
					int newAngle;
					if (angle < 90)
						newAngle = 90 + angle;
					else
						newAngle = 90 - (180 - angle);
						
					/* get the 'maximum' endpoints of the detected line to draw it */
					int xStart = x + (int)(maxLength * cosLookupTable[newAngle]);
					int xEnd = x - (int)(maxLength * cosLookupTable[newAngle]);
					int yStart = y + (int)(maxLength * sinLookupTable[newAngle]);
					int yEnd = y - (int)(maxLength * sinLookupTable[newAngle]);
					/* draw the detected line */
					originalOverlayedImage.drawOverlayLine(xStart, yStart, xEnd, yEnd, Color.GREEN);
					
					/* just test code to draw some pixels along the detected line */
//					double m = Math.tan(Math.toRadians(newAngle));
//					double x1 = x;
//					double y1 = y;
//					for (int i = 0; i < 20; i++)
//					{
//						originalOverlayedImage.setPixel((int)x1, (int)y1, Color.RED);
//						x1 += 1;
//						y1 += m;
//					}	
					
				}
			}
		}
	}


	@Override
	public HTImage getImage() {
		return originalOverlayedImage;
	}

}
