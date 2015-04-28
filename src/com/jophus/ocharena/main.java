package com.jophus.ocharena;

import ij.ImagePlus;
import ij.plugin.JpegWriter;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;

import com.jophus.ocharena.factory.OCHFileFactory;
import com.jophus.ocharena.factory.OchDataFactory;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.nn.OchDataRow;
import com.jophus.ocharena.ochfile.LineSegmentHeader;

public class main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		readLineHeader();
		//saveNeuralNetwork();
		//runProgrammaticPredictionTest();
	}
	
	private static void readLineHeader() {
		File file = new File(OcharenaSettings.dataFolder + "och" + File.separator + LineSegmentHeader.lineSegmentHeaderFilename);
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
			int length = is.readInt();
			for (int i = 0; i < length; i++) {
				char[] chars = new char[4];
				chars[0] = is.readChar();
				chars[1] = is.readChar();
				chars[2] = is.readChar();
				chars[3] = is.readChar();
				Object o = is.readObject();
				System.out.println("" + chars[0] + chars[1] + chars[2] + chars[3]);
				System.out.println("" + o.toString());
			}
			is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void saveNeuralNetwork() {
		MultiLayerPerceptron nnet = OchDataFactory.initializeNewMlp();
		nnet.randomizeWeights();
		
		System.out.println("Loading Training Data...");
		//DataSet trainingSet = OchDataFactory.readCsvTrainData(OcharenaSettings.dataFolder + "data.csv", true);
		DataSet trainingSet = OchDataFactory.loadImageTrainingData(OcharenaSettings.dataFolder + File.separator + "imgs");
		
		//trainingSet.saveAsTxt(OcharenaSettings.dataFolder + "testing.csv", ",");
		
		System.out.println("Training Neural Network...");
		nnet.learn(trainingSet);
		
		System.out.println("Saving Neural Network...");
		nnet.save(OcharenaSettings.dataFolder + "testBigNN.nnet");
	}
	
	private static void renameImages() {
		String rootDirName = OcharenaSettings.dataFolder + "imgs";
		File rootDir = new File(rootDirName);
		for (String eachCharDir : rootDir.list()) {
			File charDir = new File(rootDirName + File.separator + eachCharDir);
			if (!charDir.isDirectory()) continue;
			String[] charList = charDir.list();
			for (int i = 0; i < charList.length; i++) {
				String newName = rootDirName + File.separator + eachCharDir + "_" + i + ".jpg";
				File input = new File(rootDirName + File.separator + eachCharDir + File.separator + charList[i]);
				try {
					ImageIO.write(ImageIO.read(input), "jpg", new File(newName));
				} catch (IOException e) {
					e.printStackTrace();
				}
				input.delete();
			}
			charDir.delete();
		}
	}
	
	private static void runProgrammaticPredictionTest() {
		String imgDir = "test";
		System.out.println("Loading Neural Network...");
		NeuralNetwork nnet = NeuralNetwork.createFromFile(OcharenaSettings.dataFolder + "testSmallNN.nnet");
		
		File srcImg = new File(OcharenaSettings.dataFolder + imgDir);
		for (String eachFileName : srcImg.list()) {
			
			//System.out.println("Extracting Image Features...");
			ImagePixels imagePixels = new ImagePixels(OcharenaSettings.dataFolder + imgDir + File.separator + eachFileName);
			OchDataRow dataRow = new OchDataRow(imagePixels);
			
			//System.out.println("Testing Neural Network...");
			nnet.setInput(dataRow.getData());
			nnet.calculate();
			double[] output = nnet.getOutput();
			//System.out.println("Output: " + Arrays.toString(output));
			int bestGuess = 0;
			for (int i = 1; i < output.length; i++) {
				if (output[i] > output[bestGuess]) bestGuess = i;
			}
			if (output[bestGuess] > 0.9d)
				System.out.println("Best guess for " + eachFileName + " at " + output[bestGuess] * 100 + "% confidence: " + OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess));
		}
	}
	
	private static void oldMain() {
		//GUIController guiController = new GUIController();
		//guiController.showGUI();
		//File test = new File("KaylaFont.och");
		//OCHFile ochFile = new OCHFile("KaylaFont.och");
		//MasterSegmentHeader masterHeader = new MasterSegmentHeader(test);
		//ImagePixels pixels = ochFile.extractImagePixels(masterHeader);
		//testMethod();
		File f = new File(OcharenaSettings.dataFolder + "kaylaFont.jpg");
		File outfile = new File(OcharenaSettings.dataFolder + "a.csv");
		try {
			ImagePlus image = new ImagePlus("a", ImageIO.read(f));
			ImageProcessor processor = image.getProcessor();
			processor.findEdges();
			JpegWriter writer = new JpegWriter();
			writer.save(image, OcharenaSettings.dataFolder + "outputEdges.jpg", 100);
			//MPEG7EdgeHistogram descriptor = new MPEG7EdgeHistogram();
			//descriptor.run(image);
			//List<double[]> features = descriptor.getFeatures();
			//System.out.println(features.size());
			//for (double feature : features.get(0)) System.out.println(feature);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testMethod() {
		try {
			File ochFile = OCHFileFactory.buildOCHFromImage("KaylaFont.jpg");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    private static final Logger LOG = Logger.getLogger(main.class.getName());

}
