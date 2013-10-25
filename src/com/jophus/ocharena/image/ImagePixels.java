package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ImagePixels {

	private int imgWidth;
	private int imgHeight;
	private int valueMax = 255;
	private int[] pixels;

	public ImagePixels(String filename) {
		File img = new File(filename);
		try {
			loadPixels(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ImagePixels(File file) {
		try {
			loadPixels(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadPixels(File img) throws IOException {
		String filename = img.getAbsolutePath();
		if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
			BufferedImage bimg = ImageIO.read(new File(filename));
			this.imgWidth = bimg.getWidth();
			this.imgHeight = bimg.getHeight();
			pixels = new int[this.imgWidth * this.imgHeight];

			for (int y = bimg.getMinY(); y < bimg.getMinY() + this.imgHeight; y++) {
				for (int x = bimg.getMinX(); x < bimg.getMinX() + this.imgWidth; x++) {
					pixels[y * this.imgWidth + x] = bimg.getRGB(x, y);
				}
			}
		}
	}
	
	public int[] getPixels() {
		return this.pixels;
	}
	
	public int getWidth() {
		return this.imgWidth;
	}
	
	public int getHeight() {
		return this.imgHeight;
	}
	
	public int getMaxValue() {
		return this.valueMax;
	}
	
	public int getPixel(int x, int y) {
		return pixels[y * this.imgWidth + x];
	}
	
	public int[] getRow(int y) {
		int[] result = new int[this.imgWidth];
		for (int i = 0; i < this.imgWidth; i++) {
			result[i] = pixels[y * this.imgWidth + i];
		}
		return result;
	}
}
