package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.Rectangle;

@Deprecated
/**
 * ImageHighlight class. Outlines where detected lines and characters were found.
 * @author Joe Snee
 * deprecated. See PixelPath.draw(Graphics g)
 */
public class ImageHighlight {

	private ImagePixels img;
	private int color;
	
	public ImageHighlight() {
		
	}
	
	public void setImage(String filename) {
		this.img = new ImagePixels(filename);
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public void setColor(Color color) {
		this.color = color.getRGB();
	}
	
	public int[] outlineRect(Rectangle rect, int lineWeight) {
		int[] pixels = img.getPixelValues();
		Rectangle innerFrame = new Rectangle(rect.x + lineWeight, rect.y + lineWeight, rect.width - (2 * lineWeight), rect.height - (2 * lineWeight));
		for (int y = 0; y < img.getImageHeight(); y++) {
			for (int x = 0; x < img.getImageWidth(); x++) {
				if (rect.contains(x, y) && !innerFrame.contains(x, y)) {
					pixels[y * img.getImageWidth() + x] = this.color;
				}
			}
		}
		
		return pixels;
	}
}
