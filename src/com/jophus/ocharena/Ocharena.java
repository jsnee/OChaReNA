package com.jophus.ocharena;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;

import weka.classifiers.functions.MultilayerPerceptron;

import com.jophus.ocharena.gui.GuiController;
import com.jophus.ocharena.logging.JophLogger;

public class Ocharena {

	private final static Logger LOG = Logger.getLogger(Ocharena.class.getName());

	public static final boolean DEBUGMODE = true;
	public static NeuralNetwork neuralNet;

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
		MultiLayerPerceptron mlPerceptron = new MultiLayerPerceptron(152, 62);
		//DataSet trainingSet
		GuiController guiController = new GuiController();
		guiController.showGUI();
	}
	


}
