package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * OCRMainFrame class. The main JFrame to contain the GUI
 * @author Joe Snee
 *
 */
public class OCRMainFrame extends JFrame {
	
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 700;
	private final GuiController guiController;

	/**
	 * Constructor. Sets up the frame as needed
	 * @param guiController
	 */
	public OCRMainFrame(GuiController guiController) {
		this.guiController = guiController;
		configureFrame();
		addComponents();
	}

	private void configureFrame() {
		this.setSize(OCRMainFrame.WIDTH, OCRMainFrame.HEIGHT);
		this.setMinimumSize(new Dimension(OCRMainFrame.WIDTH, OCRMainFrame.HEIGHT));
		this.setTitle("OChaReNA - Optical Character Recognition & Notation Application");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void addComponents() {

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JTabbedPane tabPane = new OCRTabbedPane(guiController);
		panel.add(tabPane, BorderLayout.CENTER);
		
		this.add(panel);
		
	}
    private static final Logger LOG = Logger.getLogger(OCRMainFrame.class.getName());
}
