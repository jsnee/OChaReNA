package com.jophus.ocharena.gui;

import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class OCRTabbedPane extends JTabbedPane {

    private static final Logger LOG = Logger.getLogger(OCRTabbedPane.class.getName());
    
	private final GUIController guiController;

	public OCRTabbedPane(GUIController guiController) {
		this.guiController = guiController;
		
		JPanel charTracer = new CharTracePanel(this.guiController);
		//this.add("Character Tracer", charTracer);
		
		JPanel lineTracer = new LineTracePanel(this.guiController);
		this.add("Line Tracer", lineTracer);
	}
}
