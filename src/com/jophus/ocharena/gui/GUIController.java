package com.jophus.ocharena.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.ScannedDocument;
import com.jophus.ocharena.plugins.CharTracer;
import com.jophus.ocharena.plugins.LineTracer;
import com.jophus.utils.gui.JophImgFrame;

public class GUIController {
	
	private static final Logger LOG = Logger.getLogger(GUIController.class.getName());

	private MainFrame mainFrame;
	private ScannedDocument doc;

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
			LOG.log(Level.SEVERE, null, e);
		}
	}

	public void showGUI()
	{
		mainFrame.setVisible(true);
	}

	public void traceChars(File imageFile) {

	}
	
	public void traceCharTest(LineSegmentedDocument lsDoc) {
		LOG.entering(this.getClass().toString(), "traceCharTest()");
		
		CharTracer ct = new CharTracer(lsDoc);
	}

	public void traceLinesTest(String filename) {
		LOG.entering(this.getClass().toString(), "traceLinesTest()");

		doc = new ScannedDocument(filename);
		LineTracer lt = new LineTracer(filename);
		LineSegmentedDocument lsDoc = lt.getSegmentedDoc(doc);
		lsDoc.archiveLineImgs();
//		BufferedImage img = null;
//		try {
//			img = lt.getTracedImage();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		JophImgFrame tracedFrame = new JophImgFrame(img);
//		tracedFrame.showFrame();
		
		LOG.info("Done!");
		LOG.exiting(this.getClass().toString(), "traceLinesTest()");
	}

	public void traceLines(String filename) {
		LOG.entering(this.getClass().toString(), "traceLines()");
		
		LineTracer lnTracer = new LineTracer(filename);
		BufferedImage img = null;
		try {
			img = lnTracer.getTracedImage();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		try {
			ImageIO.write(img, "png", new File("C:\\Users\\Joe\\Documents\\output1.png"));
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		JophImgFrame tracedFrame = new JophImgFrame(img);

		tracedFrame.showFrame();

		LOG.info("Done!");
		LOG.exiting(this.getClass().toString(), "traceLines()");
	}
}
