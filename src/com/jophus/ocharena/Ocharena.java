package com.jophus.ocharena;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.learning.BackPropagation;

import com.jophus.ocharena.factory.OchDataFactory;
import com.jophus.ocharena.gui.GuiController;
import com.jophus.ocharena.logging.JophLogger;

/**
 * Main Class
 * @author Joe Snee
 *
 */
public class Ocharena {

	private final static Logger LOG = Logger.getLogger(Ocharena.class.getName());

	public static final boolean DEBUGMODE = true;
	// The Artificial Neural Network
	public static NeuralNetwork<BackPropagation> neuralNet;
	// Static train set; not fully implemented
	public static DataSet trainingSet = new DataSet(OcharenaSettings.NUM_ATTRIBUTES, OcharenaSettings.SUPPORTED_CHARS.length());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Setup the logger
			JophLogger.setup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Ensure data directories exist
		makeDirectories();
		// Create the saved neural network if it doesn't already exist
		File nnetFile = new File(OcharenaSettings.neuralNetworkFile);
		if (!nnetFile.exists()) {
			neuralNet = OchDataFactory.initializeNewMlp();
			neuralNet.randomizeWeights();
		} else {
			neuralNet = NeuralNetwork.createFromFile(OcharenaSettings.neuralNetworkFile);
		}
		// Display GUI
		GuiController guiController = new GuiController();
		guiController.showGUI();
	}
	

	private static void makeDirectories() {
		File data = new File(OcharenaSettings.ochFolder);
		if (!data.exists()) {
			data.mkdirs();
		}
	}

}
