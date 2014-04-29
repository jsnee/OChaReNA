package com.jophus.ocharena.plugins;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Logger;

import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.ScannedDocument;
import com.jophus.ocharena.image.ImagePixels;

public class LineTracer {
	
	private static final Logger LOG = Logger.getLogger(LineTracer.class.getName());

	private String filename;
	private final double thresholdInit = 0.82;
	private double threshold = 0.82;//0.8169934641;
	private double threshold2 = threshold;
	private int maxCombRGB = 255+255+255;
	private int maxLum = 255;
	private int minLineHeight = 10;
	private boolean grayscale = false;

	public LineTracer(String filename) {
		this.filename = filename;
	}

	public BufferedImage getTracedImage() {
		LOG.entering(this.getClass().toString(), "getTracedImage()");
		ImagePixels img = new ImagePixels(this.filename);
		if (this.grayscale)
			img.toGrayscale();
		boolean[] isLine = new boolean[img.getHeight()];
		this.threshold = this.thresholdInit;
		double diff = getRMS() / getMinMaxRMS();
		LOG.fine("Diff: " + diff);
		this.threshold = 1 - diff;
		this.threshold2 = 1 - diff/2;
		LOG.fine("Thresholds:");
		LOG.fine("\tThreshold 1 = " + this.threshold);
		LOG.fine("\tThreshold 2 = " + this.threshold2);
		double rms = getRMS();
		LOG.fine("RMS Min-Max: " + getMinMaxRMS());
		LOG.fine("Root-Mean-Square: " + rms);
		LOG.fine("Max Possible: " + img.getWidth());
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}
		
		isLine = normalizeLines(isLine);
		isLine = includeOutlyingMarks(isLine, img);

		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getHeight(); y++) {
			if (isLine[y]) {
				img.setPixel(0, y, highlight);
				img.setPixel(img.getWidth() - 1, y, highlight);
			} else if ((y != 0 && y != img.getHeight() - 1) && (isLine[y - 1] || isLine[y + 1])) {
				int[] row = new int[img.getWidth()];
				Arrays.fill(row, highlight);
				img.setRow(y, row);
			}
		}
		int ctLines = 0;
		for (boolean ln : isLine) {
			if (ln)
				ctLines++;
		}
		LOG.fine(ctLines + " lines detected in the scanned image.");

		return img.getBImg();
	}
	
	public boolean[] segmentLines() {
		ImagePixels img = new ImagePixels(this.filename);
		if (this.grayscale)
			img.toGrayscale();
		boolean[] isLine = new boolean[img.getHeight()];
		this.threshold = this.thresholdInit;
		double diff = getRMS() / getMinMaxRMS();
		LOG.fine("Diff: " + diff);
		this.threshold = 1 - diff;
		this.threshold2 = 1 - diff/2;
		LOG.fine("Thresholds:");
		LOG.fine("\tThreshold 1 = " + this.threshold);
		LOG.fine("\tThreshold 2 = " + this.threshold2);
		double rms = getRMS();
		LOG.fine("RMS Min-Max: " + getMinMaxRMS());
		LOG.fine("Root-Mean-Square: " + rms);
		LOG.fine("Max Possible: " + img.getWidth());
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}
		
		isLine = normalizeLines(isLine);
		isLine = includeOutlyingMarks(isLine, img);
		return isLine;
	}
	
	public boolean[] includeOutlyingMarks(boolean[] data, ImagePixels img) {
		LOG.fine("Searching For Outlying Marks...");
		
		for (int i = 1; i < data.length - 1; i++) {
			if (i == 0) { continue; }
			if (data[i] && !data[i-1]) { // If beginning of line
				for (int x = 0; x < img.getWidth(); x++) { // Cycle through pixels above top of line
					if (getStdLum(img.getPixel(x, i)) < (this.threshold2 * this.maxLum) &&  getStdLum(img.getPixel(x, i-1)) < (this.threshold2 * this.maxLum)) {
						data[i-1] = true;
						i-=2;
					}
				}
			} else if (data[i] && !data[i+1]) { // If end of line
				for (int x = 0; x < img.getWidth(); x++) { // Cycle through pixels below bottom of line
					if (getStdLum(img.getPixel(x, i)) < (this.threshold2 * this.maxLum) &&  getStdLum(img.getPixel(x, i+1)) < (this.threshold2 * this.maxLum)) {
						data[i+1] = true;
					}
				}
			}
		}
		
		return data;
	}

	public boolean[] normalizeLines(boolean[] data) {
		LOG.fine("Normalizing Lines...");
		boolean inLine = false;
		for (int y = 0; y < data.length; y++) { // Loop through lines
			if (!inLine && data[y]) { // If beginning of line
				
				boolean isNormalized = true;
				for (int i = 1; i < this.minLineHeight + 1; i++) {
					if (!data[y + i]) {
						isNormalized = false;
						break;
					}
				}
				if (isNormalized) { // If line normalized, continue
					inLine = true;
					continue;
				}

				int yPos = 0;
				int yBot = y;
				for (int i = y; i < data.length; i++) { // Locate the bottom of the current line
					if (!data[i]) {
						yBot = i;
						break;
					}
				}
				for (int i = 1; i < this.minLineHeight + 1; i++) { // Locate nearest line
					if ((y - i > 0) && data[y - i]) {
						yPos = -i;
						break;
					}
					if ((yBot + i < data.length) && data[yBot + i]) {
						yPos = i;
						break;
					}

				}

				if (yPos != 0) { // If there exists a nearby line
					if (yPos > 0) {
						for (int i = 0; i < yPos; i++) {
							data[yBot + i] = true;
						}
					} else if (yPos < 0) {
						for (int i = 0; i > yPos; i--) {
							data[y + i] = true;
						}
					}
					inLine = true;
					continue;
				} else {
					data[y] = false;
				}

			}
			if (inLine && !data[y]) {
				inLine = false;
				continue;
			}
		}
		return data;
	}

	public int getCombinedRGB(int pixel) {
		Color color = new Color(pixel);
		return color.getRed() + color.getGreen() + color.getBlue();
	}

	public double getStdLum(int pixel) {
		Color color = new Color(pixel);
		return (0.2126d * color.getRed()) + (0.7152d * color.getGreen()) + (0.0722d * color.getBlue());
	}

	public double getPerLum(int pixel) {
		Color color = new Color(pixel);
		return (0.299d * color.getRed()) + (0.587d * color.getGreen()) + (0.114d * color.getBlue());
	}

	public BufferedImage getStdPixelLum() {
		ImagePixels img = new ImagePixels(this.filename);
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getBImg();
	}

	public BufferedImage getPerPixelLum() {
		ImagePixels img = new ImagePixels(this.filename);
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getPerLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getBImg();
	}

	public BufferedImage getPixelCount() {
		ImagePixels img = new ImagePixels(this.filename);
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getCombinedRGB(each) < (this.threshold * this.maxCombRGB))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getBImg();
	}

	public double getMinMaxRMS() {
		ImagePixels img = new ImagePixels(this.filename);
		if (this.grayscale)
			img.toGrayscale();
		int max = 0;
		int min = img.getWidth();
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			min = (count < min ? count:min);
			max = (count > max ? count:max);
		}
		LOG.fine("Minimum: " + min);
		LOG.fine("Maximum: " + max);

		return Math.sqrt((Math.pow(max, 2.0d) + Math.pow(min, 2.0d)) / 2.0d);
	}

	public double getRMS() {
		ImagePixels img = new ImagePixels(this.filename);
		int fullCt = 0;
		if (this.grayscale)
			img.toGrayscale();
		double total = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			int[] row = img.getRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			if (count == img.getWidth()) {
				fullCt++;
				continue;
			}
			total += Math.pow(count, 2.0d);
		}

		return Math.sqrt(total / (img.getHeight() - fullCt));
	}
	
	public boolean checkLinedPaperRow(int[] row) {
		int blueCtr = 0;
		
		for (int each : row) {
			Color c = new Color(each);
			if (c.getBlue() > 175 && (c.getGreen() < 150 && c.getRed() < 150)) {
				blueCtr++;
			}
		}
		
		return (blueCtr > row.length / 2 ? true:false);
	}
	
	public LineSegmentedDocument getSegmentedDoc(ScannedDocument doc) {
		return new LineSegmentedDocument(doc, this.segmentLines());
	}


}
