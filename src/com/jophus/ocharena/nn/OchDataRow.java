package com.jophus.ocharena.nn;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.Arrays;
import java.util.List;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;

import de.lmu.ifi.dbs.jfeaturelib.features.MPEG7EdgeHistogram;
import de.lmu.ifi.dbs.jfeaturelib.features.PHOG;

/**
 * OCHDataRow class. Easy-to-use data row, to simplify extraction of data from character images
 * @author Joe Snee
 *
 */
public class OchDataRow {
	private char expectedOutput;
	private double[] data;

	/**
	 * Constructor. Generates data row, based off of csv data read from a file
	 * @param csvLine unparsed csv line
	 * @param hasExpectedOutput if the actual character is known
	 */
	public OchDataRow(String csvLine, boolean hasExpectedOutput) {
		String[] values = csvLine.split(",");
		data = new double[OcharenaSettings.NUM_ATTRIBUTES];
		if (hasExpectedOutput) parseWithExpectedOutput(values);
		else parseValues(values);
	}
	
	/**
	 * Constructor. Generates data row, based off of image data.
	 * @param imagePixels
	 */
	public OchDataRow(ImagePixels imagePixels) {
		ImageProcessor image = new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor();
		// Setup feature extractors
		MPEG7EdgeHistogram mpeg = new MPEG7EdgeHistogram();
		PHOG phog = new PHOG();
		
		// Run MPEG7 and collect data
		mpeg.run(image);
		List<double[]> features = mpeg.getFeatures();
		// Run PHOG and append data
		phog.run(image);
		features.addAll(phog.getFeatures());
		
		data = new double[features.get(0).length + features.get(1).length];
		int index = 0;
		for (int i = 0; i < features.size(); i++) {
			for (double eachFeature : features.get(i)) {
				data[index++] = eachFeature;
			}
		}
	}
	
	/**
	 * Constructor. Generates data row, based off of image data and expected character output.
	 * @param imagePixels
	 * @param expectedOutput
	 */
	public OchDataRow(ImagePixels imagePixels, char expectedOutput) {
		this.expectedOutput = expectedOutput;
		loadFeaturesFromImage(imagePixels);
	}
	
	/**
	 * Get the feature at the provided index
	 * @param index
	 * @return
	 */
	public double get(int index) {
		if (index < OcharenaSettings.NUM_ATTRIBUTES) return data[index];
		return Double.NaN;
	}
	
	/**
	 * Get the expected output character
	 * @return
	 */
	public char getExpectedOutput() {
		return expectedOutput;
	}
	
	/**
	 * Get the expected Neural Network output
	 * @return
	 */
	public double[] getExpectedOutputArray() {
		double[] result = OcharenaSettings.emptyOutputArray();
		result[OcharenaSettings.charIndex(expectedOutput)] = 1.0d;
		return result;
	}
	
	/**
	 * Get the entire data array
	 * @return
	 */
	public double[] getData() {
		return data;
	}
	
	/**
	 * Load expected output and feature data in as a String array
	 * @param values
	 */
	private void parseWithExpectedOutput(String[] values) {
		expectedOutput = values[0].charAt(0);
		for (int i = 0; i < OcharenaSettings.NUM_ATTRIBUTES; i++) data[i] = Double.parseDouble(values[i + 1]);
	}
	
	/**
	 * Load feature data in as a String array
	 * @param values
	 */
	private void parseValues(String[] values) {
		for (int i = 0; i < OcharenaSettings.NUM_ATTRIBUTES; i++) data[i] = Double.parseDouble(values[i]);
	}
	
	/**
	 * Private class handling all of the feature extraction
	 * @param imagePixels
	 */
	private void loadFeaturesFromImage(ImagePixels imagePixels) {
		// Setup the feature detectors
		MPEG7EdgeHistogram mpeg = new MPEG7EdgeHistogram();
		PHOG phog = new PHOG();

		// Run the feature detectors and get the output data
		phog.run(new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor());
		mpeg.run(new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor());
		List<double[]> features = mpeg.getFeatures();
		features.addAll(phog.getFeatures());
		
		data = new double[features.get(0).length + features.get(1).length];
		int index = 0;
		for (int i = 0; i < features.size(); i++) {
			for (double eachFeature : features.get(i)) {
				data[index++] = eachFeature;
			}
		}
	}
}
