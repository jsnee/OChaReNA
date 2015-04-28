package com.jophus.ocharena;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;

import com.jophus.ocharena.factory.OchDataFactory;
import com.jophus.ocharena.gui.GuiController;
import com.jophus.ocharena.logging.JophLogger;

public class Ocharena {

	private final static Logger LOG = Logger.getLogger(Ocharena.class.getName());

	public static final boolean DEBUGMODE = true;
	public static NeuralNetwork neuralNet;
	public static DataSet trainingSet = new DataSet(OcharenaSettings.NUM_ATTRIBUTES, OcharenaSettings.SUPPORTED_CHARS.length());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JophLogger.setup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File nnetFile = new File(OcharenaSettings.neuralNetworkFile);
		if (!nnetFile.exists()) {
			neuralNet = OchDataFactory.initializeNewMlp();
			neuralNet.randomizeWeights();
		} else {
			neuralNet = NeuralNetwork.createFromFile(OcharenaSettings.neuralNetworkFile);
		}
		GuiController guiController = new GuiController();
		guiController.showGUI();
	}
	


}
