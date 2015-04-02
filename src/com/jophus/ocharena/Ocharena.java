package com.jophus.ocharena;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.jophus.ocharena.gui.GUIController;
import com.jophus.ocharena.logging.JophLogger;

public class Ocharena {

	private final static Logger LOG = Logger.getLogger(Ocharena.class.getName());

	public static final boolean DEBUGMODE = true;

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
		GUIController guiController = new GUIController();
		guiController.showGUI();
	}
	


}
