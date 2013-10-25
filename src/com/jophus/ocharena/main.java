package com.jophus.ocharena;

import java.util.logging.Logger;

import com.jophus.ocharena.gui.GUIController;

public class main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GUIController guiController = new GUIController();
		guiController.showGUI();
	}
    private static final Logger LOG = Logger.getLogger(main.class.getName());

}
