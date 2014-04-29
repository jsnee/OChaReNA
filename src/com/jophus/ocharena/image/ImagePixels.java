package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class ImagePixels {
	
	private static final Logger LOG = Logger.getLogger(ImagePixels.class.getName());

	private int imgWidth;
	private int imgHeight;
	private int valueMax = 255;
	private int[] pixels;
	
	public ImagePixels(int width, int height) {
		this.pixels = new int[height * width];
		this.imgWidth = width;
		this.imgHeight = height;
	}

	public ImagePixels(String filename) {
		File img = new File(filename);
		try {
			loadPixels(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}
	
	public ImagePixels(File file) {
		try {
			loadPixels(file);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
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
	
	public void setPixel(int x, int y, int val) {
		pixels[y * this.imgWidth + x] = val;
	}
	
	public void setPixel(int x, int y, Color color) {
		pixels[y * this.imgWidth + x] = color.getRGB();
	}
	
	public void setRow(int y, int[] vals) {
		for (int x = 0; x < vals.length; x++) {
			pixels[y * this.imgWidth + x] = vals[x];
		}
	}
	
	public BufferedImage getBImg() {
		BufferedImage result = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				result.setRGB(x, y, pixels[y * this.imgWidth + x]);
			}
		}
		return result;
	}
	
	public ImagePixels getRowsAsNew(int[] y) {
		ImagePixels result = new ImagePixels(this.imgWidth, y.length);
		for (int i = 0; i < y.length; i++) {
			result.setRow(i, this.getRow(y[i]));
		}
		return result;
	}
	
	public void toGrayscale() {
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				Color color = new Color(pixels[y * this.imgWidth + x]);
				int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
				pixels[y * this.imgWidth + x] = (new Color(avg, avg, avg)).getRGB();
			}
		}
	}
}
