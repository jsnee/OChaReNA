package com.jophus.ocharena.document;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.jophus.ocharena.image.ImagePixels;

public class DocumentLine {
	
	private DocumentSegment line;
	private ImagePixels pixels;
	
	public DocumentLine(ImagePixels img, int[] rows) {
		this.line = new DocumentSegment(new Rectangle(0, rows[0], img.getImageWidth(), rows[rows.length - 1] - rows[0]));
		pixels = img.getRowsAsSubimage(rows);
	}
	
	public BufferedImage getLineImage() {
		return pixels.getImageAsBufferedImage();
	}
	
	public ImagePixels getLinePixels() {
		return this.pixels;
	}
	
	

}
