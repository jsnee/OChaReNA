package com.jophus.ocharena.plugins;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.image.ImagePixels;

/**
 * BasicLineTracer class. Implements line histogram based line segmentation.
 * @author Joe Snee
 *
 */
public class BasicLineTracer {
//	private static final Logger logger = Logger.getLogger(BasicLineTracer.class.getName());

	private String imageFilename;
	private int minLineHeight;

	/**
	 * Constructor.
	 * @param imageFilename
	 */
	public BasicLineTracer(String imageFilename) {
		this.imageFilename = imageFilename;
	}

	/**
	 * Generates a line histogram of the pixel rows
	 * @return The line histogram values
	 */
	public int[] generateLineHistogram() {
		ImagePixels pixels = findEdges();
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
	
	/**
	 * Detect the lines in the document
	 * @param ochDocument The document to process.
	 * @return A 2D ArrayList of Integers of detected lines
	 */
	public ArrayList<ArrayList<Integer>> detectLines(OCHDocument ochDocument) {
		// Get the number of white pixels in the inverted image per row
		int[] whitePixels = generateLineHistogram();
		// Calculate the variance of the pixels per row and then the standard deviation
		double variance = 0.0d;
		for (int each : whitePixels) variance += Math.pow(each, 2.0d);
		double stdDev = Math.sqrt(variance / whitePixels.length);
		System.out.println("Standard Deviation: " + stdDev);
		boolean[] isLine = new boolean[whitePixels.length];
		for (int i = 0; i < whitePixels.length; i++) {
			isLine[i] = whitePixels[i] > stdDev / 3;
		}

		minLineHeight = maxLineHeight(isLine);
		// Normalize the lines
		isLine = normalizeLines(isLine);
		// Initialize the detected line list
		ArrayList<ArrayList<Integer>> lineList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> eachLine = null;
		// Build the list of detected lines
		for (int i = 0; i < isLine.length; i++) {
			if (isLine[i]) {
				if (eachLine == null) eachLine = new ArrayList<Integer>();
				eachLine.add(i);
			} else if (eachLine != null) {
				lineList.add(eachLine);
				eachLine = null;
			}
		}

		if (eachLine != null) {
			lineList.add(eachLine);
			eachLine = null;
		}
		return lineList;
	}

	@Deprecated
	/**
	 * Extract the detected line images into the archived document
	 * @param ochDocument
	 */
	public void extractLines(OCHDocument ochDocument) {
		ArrayList<ArrayList<Integer>> lineList = detectLines(ochDocument);

		File lineDir = new File(OcharenaSettings.dataFolder + "lines" + File.separator);
		lineDir.mkdir();
		
		for (int i = 0; i < lineList.size(); i++) {
			try {
				ImagePixels lineImage = ochDocument.getImagePixels().getRowsAsSubimage(toIntArray(lineList.get(i)));
				File lineFile = new File(OcharenaSettings.dataFolder + "lines" + File.separator + "line-" + i + ".jpg");
				ImageIO.write(lineImage.getImageAsBufferedImage(), "jpg", lineFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ochDocument.archiveLines(lineDir);
		for (String eachFile : lineDir.list()) {
			File currentFile = new File(lineDir.getPath(), eachFile);
			currentFile.delete();
		}
		lineDir.delete();
	}

	/**
	 * Converts a list of integers into an integer array
	 * @param integerList The integer list.
	 * @return The integer array.
	 */
	private int[] toIntArray(ArrayList<Integer> integerList) {
		int[] result = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) result[i] = integerList.get(i);
		return result;
	}

	/**
	 * Use Sobel edge detection
	 * @return The edge detected result
	 */
	public ImagePixels findEdges() {
		try {
			File f = new File(imageFilename);
			ImagePlus imagePlus = new ImagePlus("edges", ImageIO.read(f));
			ImageProcessor processor = imagePlus.getProcessor();
			processor.findEdges();
			ImagePixels result = new ImagePixels(imagePlus.getBufferedImage());
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calculate the maximum line height
	 * @param isLine A boolean array of detected lines.
	 * @return The maximum line height.
	 */
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

	/**
	 * Normalize the detected lines
	 * @param data A boolean array of detected lines
	 * @return The normalized array
	 */
	public boolean[] normalizeLines(boolean[] data) {
		boolean inLine = false;
		for (int y = 0; y < data.length; y++) { // Loop through lines
			if (!inLine && data[y]) { // If beginning of line

				boolean isNormalized = true;
				for (int i = 1; i < this.minLineHeight + 1; i++) {
					if (y + i < data.length && !data[y + i]) {
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
}
