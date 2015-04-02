package com.jophus.houghtransformation;

/**
 * Hough transformation process step to calculate the edges from the grayscale image by applying a sobel filter.
 * 
 * @author SUN2K
 *
 */
class HTStepGrayscaleToEdge extends HTAbstractProcessStep {

	/**
	 * Image of step: the grayscale image -> edge image.
	 */
	private HTGrayImage edgeImage;
	
	/**
	 * @param originalImage
	 * @param htProcessData
	 */
	public HTStepGrayscaleToEdge(HTImage originalImage, HTProcessData htProcessData) {
		super(originalImage, htProcessData);
		edgeImage = null;
	}

	@Override
	public void execute() {
		if (edgeImage == null) 
		{
			edgeImage = new HTGrayImage(sourceImage);
		}
		
		for(int x = 1; x < edgeImage.getWidth()-1; x ++)
		{
			for(int y = 1; y < edgeImage.getHeight()-1; y++)  
	        {
				// calculate the gradient in y-direction
				int sobelY1 = edgeImage.getSourcePixel(x-1, y-1) + 
							  edgeImage.getSourcePixel(x, y-1) +
					      	  edgeImage.getSourcePixel(x+1, y-1);
				int sobelY2 = edgeImage.getSourcePixel(x-1, y+1) + 
							  edgeImage.getSourcePixel(x, y+1) +
							  edgeImage.getSourcePixel(x+1, y+1);				
				int sobelY = Math.abs(sobelY1 - sobelY2);
				
				// calculate the gradient in x-direction
				int sobelX1 = edgeImage.getSourcePixel(x-1, y-1) + 
							  edgeImage.getSourcePixel(x-1, y) +
							  edgeImage.getSourcePixel(x-1, y+1);
				int sobelX2 = edgeImage.getSourcePixel(x+1, y-1) + 
							  edgeImage.getSourcePixel(x+1, y) +
							  edgeImage.getSourcePixel(x+1, y+1);				
				int sobelX = Math.abs(sobelX1 - sobelX2);
				
				int res = (int)Math.sqrt(sobelX*sobelX + sobelY*sobelY);
				
//				res = 
//				edgeImage.getSourcePixel(x-1, y-1) +
//				2 * edgeImage.getSourcePixel(x-1, y) +
//				edgeImage.getSourcePixel(x-1, y+1) +
//				
//				2 * edgeImage.getSourcePixel(x, y-1) +
//				4 * edgeImage.getSourcePixel(x, y) +
//				2 * edgeImage.getSourcePixel(x, y+1) +
//				
//				edgeImage.getSourcePixel(x+1, y-1) +
//				2 * edgeImage.getSourcePixel(x+1, y) +
//				edgeImage.getSourcePixel(x+1, y+1);
//				res /= 16;
				
				edgeImage.setPixel(x, y, HTUtil.clipByte(res));
	        }
	    }
		
		// calculate the values at the left border, exclude corner pixels
		for (int y = 1; y < edgeImage.getHeight()-1; y++)
		{
			// build the gradient of the two values next right
			int borderVal = 2 * edgeImage.getPixel(1, y) - edgeImage.getPixel(2, y);
			edgeImage.setPixel(0, y, HTUtil.clipByte(borderVal));
		}
		// calculate the values at the right border, exclude corner pixels
		for (int y = 1; y < edgeImage.getHeight()-1; y++)
		{
			// build the gradient of the two values next right
			int borderVal = 2 * edgeImage.getPixel(edgeImage.getWidth()- 2, y) - 
					         edgeImage.getPixel(edgeImage.getWidth()- 3, y);
			edgeImage.setPixel(edgeImage.getWidth()- 1, y, HTUtil.clipByte(borderVal));
		}
		// calculate the values at the top border, exclude corner pixels
		for (int x = 1; x < edgeImage.getWidth() - 1; x++)
		{
			// build the gradient of the two values next right
			int borderVal = 2 * edgeImage.getPixel(x, 1) - edgeImage.getPixel(x, 2);
			edgeImage.setPixel(x, 0, HTUtil.clipByte(borderVal));
		}
		// calculate the values at the bottom border, exclude corner pixels
		for (int x = 1; x < edgeImage.getWidth() - 1; x++)
		{
			// build the gradient of the two values next right
			int borderVal = 2 * edgeImage.getPixel(x, edgeImage.getHeight() - 2 ) - 
					     	edgeImage.getPixel(x, edgeImage.getHeight() - 3);
			edgeImage.setPixel(x, edgeImage.getHeight() - 1, HTUtil.clipByte(borderVal));
		}
		
	}

	@Override
	public HTImage getImage() {
		return edgeImage;
	}

}
