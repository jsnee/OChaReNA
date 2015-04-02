package com.jophus.ocharena.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import com.jophus.houghtransformation.EHTProcessStep;
import com.jophus.houghtransformation.HTEngine;
import com.jophus.houghtransformation.HTImage;
import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.ScannedDocument;
import com.jophus.ocharena.image.ColorUtils;
import com.jophus.ocharena.image.DocumentMetadata;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.plugins.CharTracer;
import com.jophus.ocharena.plugins.LineTracer;
import com.jophus.utils.gui.JophImgFrame;

public class GUIController {
	
	private static final Logger LOG = Logger.getLogger(GUIController.class.getName());

	private OCRMainFrame mainFrame;
	private ScannedDocument doc;

	public GUIController()
	{
		setLookAndFeel();
		mainFrame = new OCRMainFrame(this);
	}

	public void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, null, e);
		}
	}
	
	public void fftTransform(String imageFilename) {
		ImagePixels pixels = new ImagePixels(imageFilename);
	}
	
	public void houghTransform(String imageFilename) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		metadata.setWritingColor(Color.black);
		metadata.setUniformBackgroundColor(false);
		ImagePixels pixels = new ImagePixels(imageFilename, metadata);
		ColorUtils.printRGBValues(pixels.getPixelValueByCoordinate(861, 66));
		//pixels.prepareImage();
		HTEngine htEngine = new HTEngine();
		htEngine.setSourceImage(pixels.getImageAsBufferedImage());
		ArrayList<EHTProcessStep> steps = new ArrayList<EHTProcessStep>();
		//steps.add(EHTProcessStep.STEP_GRAYSCALE);
		steps.add(EHTProcessStep.STEP_EDGE_DETECTION);
		//steps.add(EHTProcessStep.STEP_EDGE_TRESHOLD);
		//steps.add(EHTProcessStep.STEP_HOUGH_SPACE_TOP);
		//steps.add(EHTProcessStep.STEP_HOUGH_SPACE_CENTER);
		//steps.add(EHTProcessStep.STEP_HOUGH_SPACE_FILTERED);
		HTImage resultImage = htEngine.getHTProcessSteps(steps);
		ImagePixels pix = new ImagePixels(resultImage.getImage());

		ColorUtils.printRGBValues(pix.getPixelValueByCoordinate(861, 66));
		try {
			File file = new File("outputHough.jpg");
			ImageIO.write(pixels.getImageAsBufferedImage(), "jpg", file);
			invertImage(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		ImagePixels pxls = new ImagePixels(resultImage.getImage());
		System.out.println("I'm Here!");
		ColorUtils.printRGBValues(resultImage.getImage().getRGB(861, 66));
		JophImgFrame tracedFrame = new JophImgFrame(resultImage.getImage());

		tracedFrame.showFrame();
	}
	
	public void houghTransform2(String imageFilename) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		ImagePixels pixels = new ImagePixels(imageFilename, metadata);
		pixels.prepareImage();
		try {
			File file = new File("outputFiltered.jpg");
			ImageIO.write(pixels.getImageAsBufferedImage(), "png", file);
			traceLines(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		//JophImgFrame tracedFrame = new JophImgFrame(pixels.getImageAsBufferedImage());

		//tracedFrame.showFrame();
	}
	
	public void invertImage(String imageFilename) {
		ImagePixels pixels = new ImagePixels(imageFilename);
		ColorUtils.printRGBValues(pixels.getPixelValueByCoordinate(861, 66));
		pixels.invertColors();
		ColorUtils.printRGBValues(pixels.getPixelValueByCoordinate(861, 66));
		try {
			File file = new File("outputInverted.jpg");
			ImageIO.write(pixels.getImageAsBufferedImage(), "jpg", file);
			traceLines(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
		//JophImgFrame tracedFrame = new JophImgFrame(pixels.getImageAsBufferedImage());

		//tracedFrame.showFrame();
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
			ImageIO.write(img, "png", new File("output-" + System.currentTimeMillis() + ".png"));
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
