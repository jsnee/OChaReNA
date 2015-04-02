package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jophus.houghtransformation.EHTProcessStep;
import com.jophus.houghtransformation.HTEngine;
import com.jophus.houghtransformation.HTImage;
import com.jophus.ocharena.image.ColorUtils.ColorComponent;

public class ImagePixels {

	private static final Logger LOG = Logger.getLogger(ImagePixels.class.getName());

	private int imgWidth;
	private int imgHeight;
	private int maxPixelValue = 255;
	private int[] pixels;
	private Color averageColor = new Color(0, 0, 0);
	private int brightestColor = new Color(0, 0, 0).getRGB();
	private DocumentMetadata metadata = new DocumentMetadata();

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

	public ImagePixels(String filename, boolean muteBlue) {
		File img = new File(filename);
		metadata.setBlueLinedPaper(muteBlue);
		try {
			loadPixelsFromFile(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	public ImagePixels(String filename, DocumentMetadata metadata) {
		File img = new File(filename);
		this.metadata = metadata;
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
	
	public ImagePixels(BufferedImage bimg) {
		loadPixelsFromBufferedImage(bimg);
	}
	
	public ImagePixels(BufferedImage bimg, DocumentMetadata metadata) {
		this.metadata = metadata;
		loadPixelsFromBufferedImage(bimg);
	}
	
	public void setMetadata(DocumentMetadata metadata) {
		this.metadata = metadata;
	}

	public static int muteBlue(int rgb) {
		Color color = new Color(rgb);
		int avgRG = ((color.getRed() + color.getGreen()) / 2);
		return (color.getBlue() > avgRG ? Color.white : color).getRGB();
	}

	public static int muteRed(int rgb) {
		Color color = new Color(rgb);
		int avgRG = ((color.getRed() + color.getGreen()) / 2);
		return (color.getRed() > 150 ? Color.white : color).getRGB();
	}

	public void loadPixelsFromFile(File img) throws IOException {
		String filename = img.getAbsolutePath();
		if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".PNG") || filename.endsWith(".JPG") || filename.endsWith(".JPEG")) {
			loadPixelsFromBufferedImage(ImageIO.read(new File(filename)));
		}
	}
	
	private void loadPixelsFromBufferedImage(BufferedImage bimg) {
		this.imgWidth = bimg.getWidth();
		this.imgHeight = bimg.getHeight();
		pixels = new int[this.imgWidth * this.imgHeight];
		for (int y = bimg.getMinY(); y < bimg.getMinY() + this.imgHeight; y++) {
			for (int x = bimg.getMinX(); x < bimg.getMinX() + this.imgWidth; x++) {
				pixels[y * this.imgWidth + x] = bimg.getRGB(x, y);
				adjustAverageColor(bimg.getRGB(x, y));
				brightestColor = Math.max(brightestColor, bimg.getRGB(x, y));
			}
		}
	}

	private void adjustAverageColor(int rgbValue) {
		Color color = new Color(rgbValue);
		int r = (averageColor.getRed() + color.getRed()) / 2;
		int g = (averageColor.getGreen() + color.getGreen()) / 2;
		int b = (averageColor.getBlue() + color.getBlue()) / 2;
		averageColor = new Color(r, g, b);
	}

	private int filterOnRGB(int rgbValue) {
		Color color = new Color(rgbValue);
		double avgRG = Math.floor((double)averageColor.getRed() / (double)averageColor.getGreen() * 100) / 100;
		double avgRB = Math.floor((double)averageColor.getRed() / (double)averageColor.getBlue() * 100) / 100;
		double avgGB = Math.floor((double)averageColor.getGreen() / (double)averageColor.getBlue() * 100) / 100;

		double ratioRG = Math.floor((double)color.getRed() / (double)color.getGreen() * 100) / 100;
		double ratioRB = Math.floor((double)color.getRed() / (double)color.getBlue() * 100) / 100;
		double ratioGB = Math.floor((double)color.getGreen() / (double)color.getBlue() * 100) / 100;

		if (avgRG == ratioRG || avgRB == ratioRB || avgGB == ratioGB) {
			return Color.white.getRGB();
		}
		return rgbValue;
	}

	private int filterOnHSV(int rgbValue) {
		HSVColor difference = ColorUtils.hsvDifference(averageColor, new Color(rgbValue));
		if (difference.getHue() < 10 || difference.getSaturation() < 0.1f || difference.getValue() < 0.1f) {
			return new Color(rgbValue).brighter().getRGB();
		}
		return rgbValue;
	}
	
	public void invertColors() {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = ColorUtils.invertColor(pixels[i]); 
		}
	}
	
	public int getWhitestColor() {
		return brightestColor;
	}
	
	public BufferedImage preformHoughTransform(ArrayList<EHTProcessStep> steps) {
		HTEngine htEngine = new HTEngine();
		htEngine.setSourceImage(getImageAsBufferedImage());
		return htEngine.getHTProcessSteps(steps).getImage();
	}
	
	public void prepareImage() {
		if (brightestColor != -1) {
			ArrayList<EHTProcessStep> steps = new ArrayList<EHTProcessStep>();
			System.out.println("here!");
			steps.add(EHTProcessStep.STEP_GRAYSCALE);
			steps.add(EHTProcessStep.STEP_EDGE_DETECTION);
			//steps.add(EHTProcessStep.STEP_EDGE_TRESHOLD);
			loadPixelsFromBufferedImage(preformHoughTransform(steps));
			invertColors();
		} else {
			//filterPixelValues();
		}
	}

	public void filterPixelValues() {
		for (int i = 0; i < pixels.length; i++) {
			if (!metadata.isUniformBackgroundColor()) {
				//pixels[i] = filterOnHSV(pixels[i]);
				//pixels[i] = filterOnRGB(pixels[i]);
			}
			if (metadata.isRedMarginPaper()) {
				pixels[i] = muteRed(pixels[i]);
			}
			if (metadata.isBlueLinedPaper()) {
				pixels[i] = muteBlue(pixels[i]);
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

	public int getPixelByIndex(int index) {
		return pixels[index];
	}

	public void setPixel(int x, int y, int val) {
		pixels[y * this.imgWidth + x] = val;
	}

	public void setPixel(int x, int y, Color color) {
		pixels[y * this.imgWidth + x] = color.getRGB();
	}

	public void setPixel(int index, int rgbValue) {
		pixels[index] = rgbValue;
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
