package com.jophus.houghtransformation;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Sun2k
 *
 */
class HTGrayImage extends HTImage {

	/**
	 * image data of original pixel. Is never changed.
	 */
	private int[] imageData;
	
	/**
	 * Build a new image by duplicating the source image and converting it to grayscale.
	 * @param colorImage
	 */
	public HTGrayImage(HTImage colorImage)
	{
		img = new BufferedImage(
				colorImage.getWidth(), colorImage.getHeight(),  
	            BufferedImage.TYPE_BYTE_GRAY); 
        Graphics g = img.getGraphics();  
        g.drawImage(colorImage.getImage(), 0, 0, null);  
        g.dispose();  
        
        prepareImage();
	}
	
	/**
	 * Create a new grayscale image by given dimension.
	 * @param width request width of image
	 * @param height requested height of image
	 */
	public HTGrayImage(int width, int height)
	{
		img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY); 
		prepareImage();
	}
	
	/**
	 * Build a new image by duplicating the source image.
	 * @param htImage
	 */
	public HTGrayImage(HTGrayImage htImage)
	{
		img = HTUtil.getCopy(htImage.getImage());
		prepareImage();
	}
	
	/**
	 * Prepare the image by extracting the original source pixels into a local array.
	 * This fastens pixel access.
	 */
	private void prepareImage()
	{
		int[] outputarray= new int[img.getWidth() * img.getHeight()];
		imageData = img.getRaster().getSamples(0, 0, img.getWidth(), img.getHeight(), 0, outputarray);
	}
	
	/**
	 * Get the gray value of the original source image.
	 * Note that changes via setPixel() are not reflected by this function!
	 * @param x x position of requested pixel
	 * @param y y position of requested pixel
	 * @return the luminance gray value at the requested location, always in range [0,255].
	 */
	public int getSourcePixel(int x, int y)
	{
		if (x >= img.getWidth() || y >= img.getHeight())
			throw new HTException("Requested pixel (" + x + "," + y + ") out of bounds");
		return imageData[x + y * img.getWidth()];
	}
	
	/**
	 * Get the actual gray pixel value of the image. This might have been changed via setPixel().
	 * @param x x position of requested pixel
	 * @param y y position of requested pixel
	 * @return the luminance gray value at the requested location, always in range [0,255].
	 */
	public int getPixel(int x, int y)
	{
		if (x >= img.getWidth() || y >= img.getHeight())
			throw new HTException("Requested pixel (" + x + "," + y + ") out of bounds");
		return img.getRaster().getSample(x, y, 0);
	}
	
	/**
	 * Set the pixel value at the requested position.
	 * @param x x position of set pixel
	 * @param y y position of set pixel
	 * @param val luminance value to set, must be in range [0,255]
	 */
	public void setPixel(int x, int y, int val)
	{
		if (val < 0 || val > 255)
			throw new HTException("Color value out of range: " + val);
		if (x >= img.getWidth() || y >= img.getHeight())
			throw new HTException("Requested pixel (" + x + "," + y + ") out of bounds");
		img.getRaster().setSample(x, y, 0, val);
	}
}
