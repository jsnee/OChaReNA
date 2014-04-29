package com.jophus.ocharena.document;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.jophus.ocharena.image.ImagePixels;

public class DocumentLine {
	
	private DocumentSegment line;
	private ImagePixels pixels;
	
	public DocumentLine(ImagePixels img, int[] rows) {
		this.line = new DocumentSegment(new Rectangle(0, rows[0], img.getWidth(), rows[rows.length - 1] - rows[0]));
		pixels = img.getRowsAsNew(rows);
	}
	
	public BufferedImage getLineImage() {
		return pixels.getBImg();
	}
	
	public ImagePixels getLinePixels() {
		return this.pixels;
	}
	
	

}
