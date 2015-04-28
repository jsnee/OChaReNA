package com.jophus.ocharena.plugins;

import ij.ImagePlus;
import ij.plugin.JpegWriter;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.document.OCHFile;
import com.jophus.ocharena.document.ScannedDocument;
import com.jophus.ocharena.factory.OCHFileFactory;
import com.jophus.ocharena.image.DocumentMetadata;
import com.jophus.ocharena.image.ImagePixels;

public class LineTracer {
	
	private static final Logger LOG = Logger.getLogger(LineTracer.class.getName());

	private String filename;
	private String imageFilename;
	private final double thresholdInit = 0.82;
	private double threshold = 0.82;//0.8169934641;
	private double threshold2 = threshold;
	private int maxCombRGB = 255+255+255;
	private int maxLum = 255;
	private int minLineHeight = 10;
	private boolean grayscale = false;
	private ImagePixels imagePixels;
	private boolean muteBlue = false;

	public LineTracer(String filename) {
		if (OCHFile.isValidOCHDocumentFile(filename)) {
			this.filename = filename;
		} else {
			try {
				imageFilename = filename;
				File ochFile = OCHFileFactory.buildOCHFromImage(filename);
				this.filename = ochFile.getAbsolutePath();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public LineTracer(String filename, boolean muteBlue) {
		this.muteBlue = muteBlue;
		if (OCHFile.isValidOCHDocumentFile(filename)) {
			this.filename = filename;
		} else {
			try {
				imageFilename = filename;
				File ochFile = OCHFileFactory.buildOCHFromImage(filename);
				this.filename = ochFile.getAbsolutePath();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean[] identifyLines() {
		LOG.entering(this.getClass().toString(), "identifyLines()");
		OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		imagePixels = ochDocument.getImagePixels();
		DocumentMetadata metadata = new DocumentMetadata();
		boolean[] isLine = new boolean[imagePixels.getImageHeight()];
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
		LOG.fine("Max Possible: " + imagePixels.getImageWidth());
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			int[] row = imagePixels.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}

		isLine = includeOutlyingMarks(isLine, imagePixels);
		isLine = normalizeLines(isLine);
		isLine = segmentLines();
		minLineHeight = maxLineHeight(isLine);
		isLine = normalizeLines(isLine);
		return isLine;
	}
	
	public BufferedImage getSegmentedImage() {
		LOG.entering(this.getClass().toString(), "getTracedImage()");
		OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		imagePixels = ochDocument.getImagePixels();
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		imagePixels.setMetadata(metadata);
		imagePixels.prepareImage();
		if (this.grayscale)
			imagePixels.convertPixelsToGrayscale();
		boolean[] isLine = new boolean[imagePixels.getImageHeight()];
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
		LOG.fine("Max Possible: " + imagePixels.getImageWidth());
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			int[] row = imagePixels.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}

		isLine = includeOutlyingMarks(isLine, imagePixels);
		isLine = normalizeLines(isLine);
		isLine = segmentLines();
		minLineHeight = maxLineHeight(isLine);
		isLine = normalizeLines(isLine);

		ochDocument = new OCHDocument(filename, false);
		imagePixels = ochDocument.getImagePixels();
		
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			if (isLine[y]) {
				imagePixels.setPixel(0, y, highlight);
				imagePixels.setPixel(imagePixels.getImageWidth() - 1, y, highlight);
			} else if ((y != 0 && y != imagePixels.getImageHeight() - 1) && (isLine[y - 1] || isLine[y + 1])) {
				int[] row = new int[imagePixels.getImageWidth()];
				Arrays.fill(row, highlight);
				imagePixels.setRowPixelValues(y, row);
			}
		}
		int ctLines = 0;
		for (boolean ln : isLine) {
			if (ln)
				ctLines++;
		}
		LOG.fine(ctLines + " lines detected in the scanned image.");

		return imagePixels.getImageAsBufferedImage();
	}
	
	public BufferedImage histogramTracing() {
		OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		imagePixels = ochDocument.getImagePixels();

		int[] whitePixels = generateLineHistogram();
		double variance = 0.0d;
		for (int each : whitePixels) variance += Math.pow(each, 2.0d);
		double stdDev = Math.sqrt(variance / whitePixels.length);
		System.out.println("Standard Deviation: " + stdDev);
		boolean[] isLine = new boolean[whitePixels.length];
		for (int i = 0; i < whitePixels.length; i++) {
			isLine[i] = whitePixels[i] > stdDev / 3;
		}
		
		minLineHeight = maxLineHeight(isLine);
		isLine = normalizeLines(isLine);

		ochDocument = new OCHDocument(filename, false);
		imagePixels = ochDocument.getImagePixels();
		
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			if (isLine[y]) {
				//imagePixels.setPixel(0, y, highlight);
				//imagePixels.setPixel(imagePixels.getImageWidth() - 1, y, highlight);
			} /*else if ((y != 0 && y != imagePixels.getImageHeight() - 1) && (isLine[y - 1] || isLine[y + 1])) {
				int[] row = new int[imagePixels.getImageWidth()];
				Arrays.fill(row, highlight);
				imagePixels.setRowPixelValues(y, row);
			}*/
			else {
				imagePixels.setRowPixelColor(y, highlight);
			}
		}
		int ctLines = 0;
		for (boolean ln : isLine) {
			if (ln)
				ctLines++;
		}
		LOG.fine(ctLines + " lines detected in the scanned image.");

		return imagePixels.getImageAsBufferedImage();
	}
	
	public ImagePixels findEdgesInvertColors() {
		try {
			File f = new File(imageFilename);
			ImagePlus imagePlus = new ImagePlus("edges", ImageIO.read(f));
			ImageProcessor processor = imagePlus.getProcessor();
			processor.findEdges();
			//JpegWriter writer = new JpegWriter();
			//writer.save(imagePlus, OcharenaSettings.dataFolder + "outputEdges2.jpg", 100);
			ImagePixels result = new ImagePixels(imagePlus.getBufferedImage());
			result.invertColors();
			//File file = new File(OcharenaSettings.dataFolder + File.separator + "outputInverted2.jpg");
			//ImageIO.write(result.getImageAsBufferedImage(), "jpg", file);
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public BufferedImage getTracedImage() {
		LOG.entering(this.getClass().toString(), "getTracedImage()");
		OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		/*
		imagePixels = findEdgesInvertColors();//ochDocument.getImagePixels();
		if (this.grayscale)
			imagePixels.convertPixelsToGrayscale();
		boolean[] isLine = new boolean[imagePixels.getImageHeight()];
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
		LOG.fine("Max Possible: " + imagePixels.getImageWidth());
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			int[] row = imagePixels.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}

		isLine = includeOutlyingMarks(isLine, imagePixels);
		isLine = normalizeLines(isLine);
		isLine = segmentLines();
		minLineHeight = maxLineHeight(isLine);
		isLine = normalizeLines(isLine);
		*/

		boolean[] isLine = identifyLines();
		ochDocument = new OCHDocument(filename, false);
		imagePixels = ochDocument.getImagePixels();
		
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < imagePixels.getImageHeight(); y++) {
			if (isLine[y]) {
				imagePixels.setPixel(0, y, highlight);
				imagePixels.setPixel(imagePixels.getImageWidth() - 1, y, highlight);
			} else if ((y != 0 && y != imagePixels.getImageHeight() - 1) && ((isLine[y - 1] || isLine[y + 1]) || (y != 1 && y != imagePixels.getImageHeight() - 2 && (isLine[y - 2] || isLine[y + 2])))) {
				int[] row = new int[imagePixels.getImageWidth()];
				Arrays.fill(row, highlight);
				imagePixels.setRowPixelValues(y, row);
			}
		}
		int ctLines = 0;
		for (boolean ln : isLine) {
			if (ln)
				ctLines++;
		}
		LOG.fine(ctLines + " lines detected in the scanned image.");

		return imagePixels.getImageAsBufferedImage();
	}
	
	public int maxLineHeight(boolean[] isLine) {
		int maximumHeight = 0;
		int currentCount = 0;
		for (int i = 1; i < isLine.length; i++) {
			if (isLine[i] && isLine[i-1]) {
				currentCount++;
			} else if (!isLine[i] && isLine[i-1]) {
				currentCount++;
				maximumHeight = (currentCount > maximumHeight ? currentCount : maximumHeight);
				currentCount = 0;
			}
		}
		
		return maximumHeight;
	}
	
	public BufferedImage generateHistogramOverlay() {
		int highlight = (new Color(255, 0, 255)).getRGB();
		ImagePixels pixels = new ImagePixels(imageFilename, false);
		int[] whitePixels = generateLineHistogram();
		for (int i = 0; i < whitePixels.length; i++) {
			int[] rowPixels = pixels.getPixelRow(i);
			for (int j = 0; j < whitePixels[i]; j++) {
				rowPixels[j] = highlight;
			}
			pixels.setRowPixelValues(i, rowPixels);
		}
		return pixels.getImageAsBufferedImage();
	}
	
	public int[] generateLineHistogram() {
		ImagePixels pixels = findEdgesInvertColors();
		pixels.invertColors();
		int[] whitePixelCounts = new int[pixels.getImageHeight()];
		for (int i = 0; i < pixels.getImageHeight(); i++) {
			int whiteCount = 0;
			for (int eachPixel : pixels.getPixelRow(i)) {
				if (eachPixel == Color.WHITE.getRGB()) whiteCount++;
			}
			whitePixelCounts[i] = whiteCount;
		}
		return whitePixelCounts;
	}
	
	public boolean[] segmentLines() {
		OCHDocument ochDocument = new OCHDocument(filename);
		ImagePixels img = new ImagePixels(ochDocument.getImagePixels().getImageAsBufferedImage());
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		img.setMetadata(metadata);
		img.prepareImage();
		if (this.grayscale)
			img.convertPixelsToGrayscale();
		boolean[] isLine = new boolean[img.getImageHeight()];
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
		LOG.fine("Max Possible: " + img.getImageWidth());
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold2 * this.maxLum) || getCombinedRGB(each) < (this.threshold2 * this.maxCombRGB))
					count++;
			}

			isLine[y] = ((count >= rms) ? true:false);
		}

		isLine = includeOutlyingMarks(isLine, img);
		isLine = normalizeLines(isLine);
		return isLine;
	}
	
	public boolean[] includeOutlyingMarks(boolean[] data, ImagePixels img) {
		LOG.fine("Searching For Outlying Marks...");
		
		for (int i = 1; i < data.length - 1; i++) {
			int pixCount = 0;
			if (i <= 0) { continue; }
			if (data[i] && !data[i-1]) { // If beginning of line
				for (int x = 0; x < img.getImageWidth(); x++) { // Cycle through pixels above top of line
					if (getStdLum(img.getPixelValueByCoordinate(x, i)) < (this.threshold2 * this.maxLum) &&  getStdLum(img.getPixelValueByCoordinate(x, i-1)) < (this.threshold2 * this.maxLum)) {
						pixCount++;
					}
				}
				if (pixCount > img.getImageWidth() / 10) {
					data[i-1] = true;
					i-=2;
				}
			} else if (data[i] && !data[i+1]) { // If end of line
				for (int x = 0; x < img.getImageWidth(); x++) { // Cycle through pixels below bottom of line
					if (getStdLum(img.getPixelValueByCoordinate(x, i)) < (this.threshold2 * this.maxLum) &&  getStdLum(img.getPixelValueByCoordinate(x, i+1)) < (this.threshold2 * this.maxLum)) {
						pixCount++;
					}
				}
				if (pixCount > img.getImageWidth() / 10) {
					data[i+1] = true;
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

	//Standard luminance
	public double getStdLum(int pixel) {
		Color color = new Color(pixel);
		return (0.2126d * color.getRed()) + (0.7152d * color.getGreen()) + (0.0722d * color.getBlue());
	}

	//Perceived luminance
	public double getPerLum(int pixel) {
		Color color = new Color(pixel);
		return (0.299d * color.getRed()) + (0.587d * color.getGreen()) + (0.114d * color.getBlue());
	}

	public BufferedImage getStdPixelLum() {
		//OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		ImagePixels img = imagePixels;//ochDocument.getImagePixels();
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getImageAsBufferedImage();
	}

	public BufferedImage getPerPixelLum() {
		//OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		ImagePixels img = imagePixels;//ochDocument.getImagePixels();
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getPerLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getImageAsBufferedImage();
	}

	public BufferedImage getPixelCount() {
		//OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		ImagePixels img = imagePixels;//ochDocument.getImagePixels();
		int highlight = (new Color(255, 0, 255)).getRGB();
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getCombinedRGB(each) < (this.threshold * this.maxCombRGB))
					count++;
			}
			for (int x = 0; x < count; x++) {
				img.setPixel(x, y, highlight);
			}

		}

		return img.getImageAsBufferedImage();
	}

	public double getMinMaxRMS() {
		//OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		ImagePixels img = imagePixels;//ochDocument.getImagePixels();
		if (this.grayscale)
			img.convertPixelsToGrayscale();
		int max = 0;
		int min = img.getImageWidth();
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
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
		//OCHDocument ochDocument = new OCHDocument(filename, muteBlue);
		ImagePixels img = imagePixels;//ochDocument.getImagePixels();
		int fullCt = 0;
		if (this.grayscale)
			img.convertPixelsToGrayscale();
		double total = 0;
		for (int y = 0; y < img.getImageHeight(); y++) {
			int[] row = img.getPixelRow(y);
			int count = 0;
			for (int each : row) {
				if (getStdLum(each) < (this.threshold * this.maxLum))
					count++;
			}
			if (count == img.getImageWidth()) {
				fullCt++;
				continue;
			}
			total += Math.pow(count, 2.0d);
		}

		return Math.sqrt(total / (img.getImageHeight() - fullCt));
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

	public ImagePixels getImagePixels() {
		return imagePixels;
	}

}
