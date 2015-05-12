package com.jophus.ocharena.plugins;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.image.ImagePixels;

@Deprecated
/**
 * CharTracer class. Testing class used to test various character isolation methods.
 * @author Joe Snee
 * deprecated. See BasicCharTracer
 *
 */
public class CharTracer {

	private static final Logger LOG = Logger.getLogger(CharTracer.class.getName());
	
	private LineSegmentedDocument doc;
	private String filename;
	private boolean muteBlue = false;
	private int[][] lineData = new int[0][lineDataLength];
	private ImagePixels imagePixels;
	private int averageLineHeight;
	private static final int lineDataLength = 2;
	private double standardDeviation;
	
	public CharTracer(LineSegmentedDocument doc) {
		this.doc = doc;
	}
	
	public CharTracer(String filename) {
		this.filename = filename;
	}
	
	public void histogramTracing() {
		LineTracer lineTracer = new LineTracer(filename, muteBlue);
		OCHDocument ochDocument = new OCHDocument(filename, false);
		imagePixels = ochDocument.getImagePixels();
		int[] whitePixels = lineTracer.generateLineHistogram();
		double variance = 0.0d;
		for (int each : whitePixels) variance += Math.pow(each, 2.0d);
		double stdDev = Math.sqrt(variance / whitePixels.length);
		boolean[] isLine = new boolean[whitePixels.length];
		for (int i = 0; i < whitePixels.length; i++) {
			isLine[i] = whitePixels[i] > stdDev;
		}
		
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
		try {
			ImageIO.write(imagePixels.getImageAsBufferedImage(), "png", new File("testingImage.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void basicTracing() {
		LineTracer lineTracer = new LineTracer(filename, muteBlue);
		boolean[] isLine = lineTracer.identifyLines();
		imagePixels = lineTracer.getImagePixels();
		extractLineData(isLine);
		double variance = 0.0d;
		for (int[] each : lineData) variance += each[1] * each[1] * 1.0d;
		variance = variance / lineData.length;
		standardDeviation = Math.sqrt(variance);
		//lineData = normalize(lineData);
		
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
		try {
			ImageIO.write(imagePixels.getImageAsBufferedImage(), "png", new File("testingImage.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedImage testTraceChars() {
		LOG.entering(this.getClass().toString(), "testTraceChars()");
		
		BufferedImage bImg;
		
		return new BufferedImage(0, 0, 0);
	}
	
	public void segmentChars() {
		
	}
	
	public boolean[] normalizeLines(boolean[] isLine) {
		int eachLineHeight = 0;
		for (int i = 0; i < isLine.length; i++) {
			if (i == 0) {
				eachLineHeight = (isLine[i] ? 1 : 0);
				continue;
			}
			
		}
		return isLine;
	}
	
	private int[][] normalize(int[][] isLine) {
		for (int i = 0; i < isLine.length; i++) {
			if (isLine[i][1] < averageLineHeight - standardDeviation) {
				int lineUp = 9999;
				int lineDown = 9999;
				if (i != 0) lineUp = isLine[i][0] - isLine[i - 1][0] + isLine[i - 1][1];
				if (i != isLine.length - 1) lineDown = isLine[i + 1][0] - isLine[i][0] + isLine[i][1];
				
				if (lineUp < lineDown) {
					int lineHeight = isLine[i][0] + isLine[i][1] - isLine[i - 1][0];
					if (lineHeight < averageLineHeight + standardDeviation) {
						int[][] temp = new int[isLine.length - 1][lineDataLength];
						isLine[i - 1][1] = lineHeight;
						System.arraycopy(isLine, 0, temp, 0, i - 1);
						System.arraycopy(isLine, i + 1, temp, i, isLine.length - 1 - i);
						return normalize(temp);
					}
				} else if (lineDown < lineUp) {
					int lineHeight = isLine[i + 1][0] + isLine[i + 1][1] - isLine[i][0] ;
					if (lineHeight < averageLineHeight + standardDeviation) {
						int[][] temp = new int[isLine.length - 1][lineDataLength];
						isLine[i][1] = lineHeight;
						System.arraycopy(isLine, 0, temp, 0, i);
						System.arraycopy(isLine, i + 2, temp, i + 1, isLine.length - 2 - i);
						return normalize(temp);
					}
				}
			}
		}
		return isLine;
	}
	
	private void addLineData(int[] lineData) {
		int[][] temp = new int[this.lineData.length + 1][lineDataLength];
		System.arraycopy(this.lineData, 0, temp, 0, this.lineData.length);
		temp[this.lineData.length] = lineData;
		this.lineData = temp;
	}
	
	private void extractLineData(boolean[] isLine) {
		int totalLines = 0;
		int lineCount = 0;
		boolean lastLine = false;
		int[] eachLine = new int[lineDataLength];
		for (int i = 0; i < isLine.length; i++) {
			if (!lastLine && isLine[i]) eachLine[0] = i;
			if (lastLine && !isLine[i]) {
				eachLine[1] = i - eachLine[0];
				addLineData(eachLine);
				lineCount++;
			}
			lastLine = isLine[i];
			if (lastLine) totalLines++;
		}
		if (lastLine) lineCount++;
		averageLineHeight = totalLines / lineCount;
	}
	
}
