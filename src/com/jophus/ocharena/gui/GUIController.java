package com.jophus.ocharena.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import com.jophus.ocharena.plugins.LineTracer;
import com.jophus.utils.gui.JophImgFrame;

public class GUIController {

	private MainFrame mainFrame;
	
	public GUIController()
	{
		setLookAndFeel();
		mainFrame = new MainFrame(this);
	}
	
	public void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, e);
		}
	}
	
	public void showGUI()
	{
		mainFrame.setVisible(true);
	}
	
	public void traceChars(File imageFile) {
		
	}
	
	public void traceLines(File imageFile) {
		LineTracer lnTracer = new LineTracer();
		BufferedImage img = lnTracer.getTracedImage(imageFile);
		
		JophImgFrame tracedFrame = new JophImgFrame(img);
		tracedFrame.showFrame();
	}
	
	private static final Logger LOG = Logger.getLogger(GUIController.class.getName());
}
