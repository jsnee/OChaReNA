package com.jophus.houghtransformation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;


/**
 * Super Image class encapsulating a buffered image.
 * 
 * @author Sun2k
 *
 */
public abstract class HTImage {

	/**
	 * internal image object
	 */
	protected BufferedImage img;
	
	/**
	 * @return internal image object
	 */
	public BufferedImage getImage()
	{
		return img;
	}
	
	/**
	 * @return width  of image
	 */
	public int getWidth()
	{
		if (img == null)
		{
			throw new HTException("Img is null");
		}
		return img.getWidth();
	}
	
	/**
	 * @return height of image
	 */
	public int getHeight()
	{
		if (img == null)
		{
			throw new HTException("Img is null");
		}
		return img.getHeight();
	}

	
	/**
	 * Draws a string on the image.
	 * @param x x-coordinate of string to draw
	 * @param y y-coordinate of string to draw
	 * @param str string to draw
	 * @param font font to use for drawing
	 */
	public void drawOverlayString(int x, int y, String str, Font font)
	{
		if (img == null)
		{
			throw new HTException("Img is null");
		}
		Graphics g = img.getGraphics();
		Font backupFont = g.getFont();
		g.setFont(font);
		g.drawString(str, x, y);
		g.setFont(backupFont);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param str
	 * @param font
	 */
	public void drawOverlayStringRotated(int x, int y, String str, Font font)
	{
		if (img == null)
		{
			throw new HTException("Img is null");
		}
		Graphics2D g = (Graphics2D)img.getGraphics();
		Font backupFont = g.getFont();
		g.setFont(font);
		
		FontMetrics metrics = g.getFontMetrics(font);
		
	    AffineTransform at = new AffineTransform();
	    at.setToRotation(-Math.PI/2.0, x + metrics.stringWidth(str)/2.0, y + metrics.getHeight()/2.0);
	    g.setTransform(at);
	    
		g.drawString(str, x, y);
		
		g.setFont(backupFont);
	}
		
	/**
	 * Draws a line on the image. This line is not drawn into the image, thus the image is 
	 * not modified; it's just drawn overlayed.
	 * @param x1 x-coordinate of start point of line.
	 * @param y1 y-coordinate of start point of line.
	 * @param x2 x-coordinate of end point of line.
	 * @param y2 y-coordinate of end point of line.
	 * @param color color to draw line.
	 */
	public void drawOverlayLine(int x1, int y1, int x2, int y2, Color color)
	{
		if (img == null)
		{
			throw new HTException("Img is null");
		}
		Graphics g = img.getGraphics();
		Color oldColor = g.getColor();
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
		g.setColor(oldColor);
	}

}
