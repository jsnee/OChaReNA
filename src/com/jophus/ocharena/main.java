package com.jophus.ocharena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import com.jophus.ocharena.document.OCHFile;
import com.jophus.ocharena.factory.OCHFileFactory;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.ochfile.MasterSegmentHeader;

public class main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//GUIController guiController = new GUIController();
		//guiController.showGUI();
		//File test = new File("KaylaFont.och");
		//OCHFile ochFile = new OCHFile("KaylaFont.och");
		//MasterSegmentHeader masterHeader = new MasterSegmentHeader(test);
		//ImagePixels pixels = ochFile.extractImagePixels(masterHeader);
		testMethod();
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
