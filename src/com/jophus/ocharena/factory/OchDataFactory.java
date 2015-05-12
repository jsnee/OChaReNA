package com.jophus.ocharena.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PixelPath;
import com.jophus.ocharena.nn.OchDataRow;

/**
 * Factory class for reading and generating Neural Net data from various sources.
 * @author Joe Snee
 *
 */
public class OchDataFactory {

	/**
	 * Generate dataset from a .csv file
	 * @param filename
	 * @param hasHeaders the first row contains column headers
	 * @return the generated dataset
	 */
	public static DataSet readCsvTrainData(String filename, boolean hasHeaders) {
		File dataFile = new File(filename);

		if (!filename.endsWith(OcharenaSettings.CSV_EXTENSION) || !dataFile.exists()) return null;

		DataSet trainingSet = new DataSet(OcharenaSettings.NUM_ATTRIBUTES, OcharenaSettings.SUPPORTED_CHARS.length()); 

		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String eachLine = br.readLine();
			if (hasHeaders) eachLine = br.readLine();
			while (eachLine != null) {
				OchDataRow dataRow = new OchDataRow(eachLine, true);
				double[] expectedOutput = OcharenaSettings.emptyOutputArray();
				expectedOutput[OcharenaSettings.charIndex(dataRow.getExpectedOutput())] = 1.0d;
				trainingSet.addRow(dataRow.getData(), expectedOutput);
				eachLine = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return trainingSet;
	}
	
	/**
	 * Generates a dataset from detected characters
	 * @param imagePixels the original image
	 * @param pixelPath the character path
	 * @param expectedCharacter
	 * @return the generated dataset
	 */
	public static DataSet loadPixelPathTrainData(ImagePixels imagePixels, PixelPath pixelPath, char expectedCharacter) {
		DataSet trainingSet = new DataSet(OcharenaSettings.NUM_ATTRIBUTES, OcharenaSettings.SUPPORTED_CHARS.length()); 
		
		OchDataRow dataRow = new OchDataRow(imagePixels.getPixelsFromPixelPath(pixelPath), expectedCharacter);
		trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
		return trainingSet;
	}
	
	/**
	 * Bulk training method. All character images should be in a directory, with the first letter of their filename being the expected character  
	 * @param imageDirectory
	 * @return the generated dataset
	 */
	public static DataSet loadImageTrainingData(String imageDirectory) {
		File srcImg = new File(imageDirectory);
		if (!srcImg.isDirectory()) return null;
		DataSet trainingSet = new DataSet(OcharenaSettings.NUM_ATTRIBUTES, OcharenaSettings.SUPPORTED_CHARS.length());
		// Loop through each file in the directory
		for (String eachFileName : srcImg.list()) {
			
			System.out.println("Extracting Image Features From: " + eachFileName);
			ImagePixels imagePixels = new ImagePixels(imageDirectory + File.separator + eachFileName);
			// Add image as new data row; expected character as the first character in the filename
			OchDataRow dataRow = new OchDataRow(imagePixels, eachFileName.charAt(0));
			
			trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
		}
		return trainingSet;
	}

	/**
	 * Generate a new Multilayer Perceptron
	 * @return the generated perceptron
	 */
	public static MultiLayerPerceptron initializeNewMlp() {
		List<Integer> layerNeurons = new ArrayList<Integer>();
		layerNeurons.add(OcharenaSettings.NUM_ATTRIBUTES);
		layerNeurons.add((OcharenaSettings.NUM_ATTRIBUTES + OcharenaSettings.SUPPORTED_CHARS.length()) / 2);
		layerNeurons.add(OcharenaSettings.SUPPORTED_CHARS.length());
		MultiLayerPerceptron nnet = new MultiLayerPerceptron(layerNeurons, TransferFunctionType.SIGMOID);
		return nnet;
	}
}
