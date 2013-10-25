package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.Rectangle;

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
		int[] pixels = img.getPixels();
		Rectangle innerFrame = new Rectangle(rect.x + lineWeight, rect.y + lineWeight, rect.width - (2 * lineWeight), rect.height - (2 * lineWeight));
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (rect.contains(x, y) && !innerFrame.contains(x, y)) {
					pixels[y * img.getWidth() + x] = this.color;
				}
			}
		}
		
		return pixels;
	}
}
