package com.jophus.houghtransformation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Represents a colored image object, used for loading the original image.
 * 
 * @author Sun2k
 *
 */
public class HTColorImage extends HTImage {

	/**
	 * Constructor to build an HTColorImage from a buffered image.
	 * All properties of the given image are taken.
	 * @param bImage
	 */
	public HTColorImage(BufferedImage bImage)
	{
		if (bImage == null)
		{
			throw new HTException("Image is null");
		}	
		img = HTUtil.getCopy(bImage);
	}
	
	/**
	 * Constructor to build an HTColorImage from a file.
	 * @param filename complete file path of image to load.
	 */
	public HTColorImage(String filename)
	{
		try {
			img = ImageIO.read(new File(filename));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, filename + "could not be loaded");
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Constructor to build an HTColorImage from a HTColorImage using deep copy.
	 * @param htImage HTColorImage to copy to a new image.
	 */
	public HTColorImage(HTColorImage htImage)
	{
		img = HTUtil.getCopy(htImage.getImage());
	}
	
	/**
	 * Constructor to build an HTColorImage from a general HTImage.
	 * @param sourceImage image to copy for the new image.
	 */
	public HTColorImage(HTImage sourceImage) {
		this(sourceImage.getImage());
	}

	/**
	 * Set a single pixel to the given color on the image.
	 * @param x x-coordinate of pixel to set.
	 * @param y y-coordinate of pixel to set.
	 * @param color color to set pixel to.
	 */
	public void setPixel(int x, int y, Color color)
	{
		setPixel(x, y, color, 1);
	}
	
	public void setPixel(int x, int y, Color color, int size)
	{
		Graphics g = img.getGraphics();
		Color oldColor = g.getColor();
		g.setColor(color);
		g.drawRect(x, y, size, size);
		g.setColor(oldColor);
	}
	
}
