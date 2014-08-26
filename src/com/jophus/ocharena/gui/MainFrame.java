package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
	
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 700;
	private final GUIController guiController;

	public MainFrame(GUIController guiController)
	{
		this.guiController = guiController;
		configureFrame();
		addComponents();
	}

	private void configureFrame() {

		this.setSize(MainFrame.WIDTH, MainFrame.HEIGHT);
		this.setMinimumSize(new Dimension(MainFrame.WIDTH, MainFrame.HEIGHT));
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
    private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());
}
