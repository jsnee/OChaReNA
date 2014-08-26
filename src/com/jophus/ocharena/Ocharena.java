package com.jophus.ocharena;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Core;

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
			e.printStackTrace();
			System.err.println("Error Loading Log File");
		}
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		GUIController guiController = new GUIController();
		guiController.showGUI();
	}

}
