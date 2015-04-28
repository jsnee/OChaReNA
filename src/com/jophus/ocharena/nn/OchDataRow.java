package com.jophus.ocharena.nn;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.Arrays;
import java.util.List;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;

import de.lmu.ifi.dbs.jfeaturelib.features.MPEG7EdgeHistogram;
import de.lmu.ifi.dbs.jfeaturelib.features.PHOG;

public class OchDataRow {
	private char expectedOutput;
	private double[] data;

	public OchDataRow(String csvLine, boolean hasExpectedOutput) {
		String[] values = csvLine.split(",");
		data = new double[OcharenaSettings.NUM_ATTRIBUTES];
		if (hasExpectedOutput) parseWithExpectedOutput(values);
		else parseValues(values);
	}
	
	public OchDataRow(ImagePixels imagePixels) {
		ImageProcessor image = new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor();
		MPEG7EdgeHistogram mpeg = new MPEG7EdgeHistogram();
		PHOG phog = new PHOG();
		
		mpeg.run(image);
		List<double[]> features = mpeg.getFeatures();
		phog.run(image);
		features.addAll(phog.getFeatures());
		
		data = new double[features.get(0).length + features.get(1).length];
		int index = 0;
		for (int i = 0; i < features.size(); i++) {
			for (double eachFeature : features.get(i)) {
				data[index++] = eachFeature;
			}
		}
		//System.out.println("MPEG7 Features: " + Arrays.toString(features.get(0)));
		//System.out.println("PHOG Features: " + Arrays.toString(features.get(1)));
		//System.out.println("Features :" + Arrays.toString(data));
	}
	
	public OchDataRow(ImagePixels imagePixels, char expectedOutput) {
		this.expectedOutput = expectedOutput;
		loadFeaturesFromImage(imagePixels);
	}
	
	public double get(int index) {
		if (index < OcharenaSettings.NUM_ATTRIBUTES) return data[index];
		return Double.NaN;
	}
	
	public char getExpectedOutput() {
		return expectedOutput;
	}
	
	public double[] getExpectedOutputArray() {
		double[] result = OcharenaSettings.emptyOutputArray();
		result[OcharenaSettings.charIndex(expectedOutput)] = 1.0d;
		return result;
	}
	
	public double[] getData() {
		return data;
	}
	
	private void parseWithExpectedOutput(String[] values) {
		expectedOutput = values[0].charAt(0);
		for (int i = 0; i < OcharenaSettings.NUM_ATTRIBUTES; i++) data[i] = Double.parseDouble(values[i + 1]);
	}
	
	private void parseValues(String[] values) {
		for (int i = 0; i < OcharenaSettings.NUM_ATTRIBUTES; i++) data[i] = Double.parseDouble(values[i]);
	}
	
	private void loadFeaturesFromImage(ImagePixels imagePixels) {
		MPEG7EdgeHistogram mpeg = new MPEG7EdgeHistogram();
		PHOG phog = new PHOG();

		phog.run(new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor());
		mpeg.run(new ImagePlus("character", imagePixels.getImageAsBufferedImage()).getProcessor());
		List<double[]> features = mpeg.getFeatures();
		features.addAll(phog.getFeatures());
		
		data = new double[features.get(0).length + features.get(1).length];
		//System.out.println("Features size: " + data.length);
		int index = 0;
		for (int i = 0; i < features.size(); i++) {
			for (double eachFeature : features.get(i)) {
				data[index++] = eachFeature;
			}
		}
	}
}
