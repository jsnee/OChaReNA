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
	private int maxPixelValue = 255;
	private int[] pixels;
	
	public ImagePixels(int width, int height) {
		this.pixels = new int[height * width];
		this.imgWidth = width;
		this.imgHeight = height;
	}

	public ImagePixels(String filename) {
		File img = new File(filename);
		try {
			loadPixelsFromFile(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}
	
	public ImagePixels(File file) {
		try {
			loadPixelsFromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	public void loadPixelsFromFile(File img) throws IOException {
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
	
	public int[] getPixelValues() {
		return this.pixels;
	}
	
	public int getImageWidth() {
		return this.imgWidth;
	}
	
	public int getImageHeight() {
		return this.imgHeight;
	}
	
	public int getMaxPixelValue() {
		return this.maxPixelValue;
	}
	
	public int getPixelValueByCoordinate(int x, int y) {
		return pixels[y * this.imgWidth + x];
	}
	
	public int[] getPixelRow(int yIndex) {
		int[] result = new int[this.imgWidth];
		for (int i = 0; i < this.imgWidth; i++) {
			result[i] = pixels[yIndex * this.imgWidth + i];
		}
		return result;
	}
	
	public void setPixel(int x, int y, int val) {
		pixels[y * this.imgWidth + x] = val;
	}
	
	public void setPixel(int x, int y, Color color) {
		pixels[y * this.imgWidth + x] = color.getRGB();
	}
	
	public void setRowPixelValues(int yIndex, int[] rowPixelValues) {
		for (int x = 0; x < rowPixelValues.length; x++) {
			pixels[yIndex * this.imgWidth + x] = rowPixelValues[x];
		}
	}
	
	public BufferedImage getImageAsBufferedImage() {
		BufferedImage result = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				result.setRGB(x, y, pixels[y * this.imgWidth + x]);
			}
		}
		return result;
	}
	
	public ImagePixels getRowsAsSubimage(int[] yIndex) {
		ImagePixels result = new ImagePixels(this.imgWidth, yIndex.length);
		for (int i = 0; i < yIndex.length; i++) {
			result.setRowPixelValues(i, this.getPixelRow(yIndex[i]));
		}
		return result;
	}
	
	public void convertPixelsToGrayscale() {
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				Color color = new Color(pixels[y * this.imgWidth + x]);
				int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
				pixels[y * this.imgWidth + x] = (new Color(avg, avg, avg)).getRGB();
			}
		}
	}
}
