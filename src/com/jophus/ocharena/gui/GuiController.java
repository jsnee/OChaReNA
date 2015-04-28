package com.jophus.ocharena.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.neuroph.core.NeuralNetwork;

import com.jophus.houghtransformation.EHTProcessStep;
import com.jophus.houghtransformation.HTEngine;
import com.jophus.houghtransformation.HTImage;
import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.LineSegmentedDocument;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.document.OCHFile;
import com.jophus.ocharena.document.ScannedDocument;
import com.jophus.ocharena.image.ColorUtils;
import com.jophus.ocharena.image.DetectedLines;
import com.jophus.ocharena.image.DocumentMetadata;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PathManagerStack.PathManagerType;
import com.jophus.ocharena.nn.OchDataRow;
import com.jophus.ocharena.plugins.BasicCharTracer;
import com.jophus.ocharena.plugins.BasicLineTracer;
import com.jophus.ocharena.plugins.CharTracer;
import com.jophus.ocharena.plugins.LineTracer;
import com.jophus.util.PannableImageFrame;
import com.jophus.util.PannableImageFrame.ListenerState;
import com.jophus.utils.gui.JophImgFrame;

public class GuiController {

	private static final Logger LOG = Logger.getLogger(GuiController.class.getName());

	private OCRMainFrame mainFrame;
	private ScannedDocument doc;
	private OCHDocument ochDoc;

	public GuiController()
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
	
	public void loadOchDocument(String ochDocumentFilename) {
		ochDoc = new OCHDocument(ochDocumentFilename);
	}

	public void processImage(String imageFilename) {
		LOG.entering(this.getClass().toString(), "processImage()");

		ochDoc = new OCHDocument(imageFilename, false);
		BasicLineTracer lineTracer = new BasicLineTracer(imageFilename);
		lineTracer.extractLines(ochDoc);
		JOptionPane.showMessageDialog(null, "Done!");
	}

	public void processImageGuided(String imageFilename) {
		LOG.entering(this.getClass().toString(), "processImageGuided()");

		ochDoc = new OCHDocument(imageFilename, false);
		BasicLineTracer lineTracer = new BasicLineTracer(imageFilename);
		try {
			BufferedImage bimg = ImageIO.read(new File(imageFilename));
			PannableImageFrame imgFrame = new PannableImageFrame("Line Selection", bimg);
			imgFrame.setGuiController(this);
			imgFrame.setDetectedLines(new DetectedLines(bimg.getWidth(), bimg.getHeight(), lineTracer.detectLines(ochDoc)));
			imgFrame.setDisplayPathType(PathManagerType.DetectedLines);
			imgFrame.setVisible(true);
			int continueProcess = JOptionPane.showConfirmDialog(null, "Select Line To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
			if (continueProcess == JOptionPane.YES_OPTION) {
				imgFrame.setListenerState(ListenerState.SelectPath);
			} else {

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processLinesGuided() {
		BasicCharTracer charTracer = new BasicCharTracer(ochDoc);
		PathManager charPaths = charTracer.detectChars();
		BufferedImage bimg = ochDoc.getImagePixels().getImageAsBufferedImage();
		PannableImageFrame imgFrame = new PannableImageFrame("Character Selection", bimg);
		imgFrame.setGuiController(this);
		imgFrame.setDetectedChars(charPaths);
		imgFrame.setDisplayPathType(PathManagerType.DetectedCharacters);
		imgFrame.setVisible(true);
		int continueProcess = JOptionPane.showConfirmDialog(null, "Select Char To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
		if (continueProcess == JOptionPane.YES_OPTION) {
			imgFrame.setListenerState(ListenerState.SelectPath);
		} else {
			guessChars(charPaths);
		}
	}

	public void saveCharPaths(PathManager pathManager) {
	}

	public void archiveLines(PathManager linePaths) {
		File lineDir = new File(OcharenaSettings.dataFolder + "lines" + File.separator);
		lineDir.mkdir();
		try {
			File lineSegmentHeaderFile = File.createTempFile("lines", OcharenaSettings.OCH_HEADER_EXTENSION);
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(lineSegmentHeaderFile));
			os.writeInt(linePaths.size());
			for (int i = 0; i < linePaths.size(); i++) {
				os.writeChar(OCHFile.NEWLINE_CHAR);
				os.writeChar(OCHFile.HYPHEN_CHAR);
				os.writeChar(linePaths.getPath(i).getSerialFlag().getCharCode());
				os.writeChar(OCHFile.SPACE_CHAR);
				linePaths.getPath(i).writePixelPath(os);
				ImagePixels lineImage = ochDoc.getImagePixels().getRowsAsSubimage(linePaths.getPath(i).getYIndicies());
				File lineFile = new File(OcharenaSettings.dataFolder + "lines" + File.separator + "line-" + i + ".jpg");
				ImageIO.write(lineImage.getImageAsBufferedImage(), "jpg", lineFile);
			}
			os.close();
			ochDoc.archiveLines(lineDir);
			ochDoc.archiveLineHeader(lineSegmentHeaderFile);
			lineSegmentHeaderFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String eachFile : lineDir.list()) {
			File currentFile = new File(lineDir.getPath(), eachFile);
			currentFile.delete();
		}
		lineDir.delete();
		BasicCharTracer charTracer = new BasicCharTracer(ochDoc);
		Object[] options = { "Guided Training", "Autodetect", "Autodetect From Now On" };
		int continueProcess = JOptionPane.showOptionDialog(null, "How do you want to detect the characters?", "Character Processing", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		if (continueProcess == JOptionPane.YES_OPTION) { // Guided Training
			//processLineGuided();
		} else if (continueProcess == JOptionPane.NO_OPTION) { // Autodetect
			processLinesGuided();
		} else if (continueProcess == JOptionPane.CANCEL_OPTION) { // Autodetect permanently

		}
		JOptionPane.showMessageDialog(null, "Done!");
	}

	public void archiveChars(PathManager charPaths) {
		File charDir = new File(OcharenaSettings.dataFolder + "chars" + File.separator);
		charDir.mkdir();

		for (int i = 0; i < charPaths.size(); i++) {
			try {
				ImagePixels charImage = ochDoc.getImagePixels().getRowsAsSubimage(charPaths.getPath(i).getYIndicies());
				File charFile = new File(OcharenaSettings.dataFolder + "chars" + File.separator + "char-" + i + ".jpg");
				ImageIO.write(charImage.getImageAsBufferedImage(), "jpg", charFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ochDoc.archiveChars(charDir);
		for (String eachFile : charDir.list()) {
			File currentFile = new File(charDir.getPath(), eachFile);
			currentFile.delete();
		}
		charDir.delete();
		JOptionPane.showMessageDialog(null, "Done!");
	}
	
	public void guessChars(PathManager charPaths) {
		String guesses = "";
		System.out.println("Loading Neural Network...");
		NeuralNetwork nnet = NeuralNetwork.createFromFile(OcharenaSettings.dataFolder + "testNN.nnet");
		for (int i = 0; i < charPaths.size(); i++) {
			if (charPaths.getPath(i).getIsSpace()) {
				System.out.println("Space detected!");
				guesses += " ";
				continue;
			}
			System.out.println("Extracting Image Features...");
			System.out.println("Width: " + ochDoc.getImagePixels().getImageWidth());
			System.out.println("Height: " + ochDoc.getImagePixels().getImageHeight());
			ImagePixels imagePixels = ochDoc.getImagePixels().getPixelsFromPixelPath(charPaths.getPath(i));
			OchDataRow dataRow = new OchDataRow(imagePixels);
			
			System.out.println("Testing Neural Network...");
			nnet.setInput(dataRow.getData());
			nnet.calculate();
			double[] output = nnet.getOutput();
			System.out.println("Output: " + Arrays.toString(output));
			int bestGuess = 0;
			for (int j = 1; j < output.length; j++) {
				if (output[j] > output[bestGuess]) bestGuess = j;
			}
			if (output[bestGuess] > 0.9d) {
				System.out.println("Best guess for " + i + " at " + output[bestGuess] * 100 + "% confidence: " + OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess));
				guesses += OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess);
			} else {
				guesses += OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess);
//				guesses += "_";
			}
		}
		System.out.println("Here is my guess: \n" + guesses);
	}

	public void fftTransform(String imageFilename) {
		ImagePixels pixels = new ImagePixels(imageFilename);
		ochDoc = new OCHDocument(imageFilename, false);

		BasicLineTracer lineTracer = new BasicLineTracer(imageFilename);
		//lineTracer.extractLines();
		try {
			BufferedImage bimg = ImageIO.read(new File(imageFilename));
			PannableImageFrame imgFrame = new PannableImageFrame("Line Selection", bimg);
			imgFrame.setGuiController(this);
			imgFrame.setDetectedLines(new DetectedLines(bimg.getWidth(), bimg.getHeight(), lineTracer.detectLines(ochDoc)));
			imgFrame.setDisplayPathType(PathManagerType.DetectedLines);
			imgFrame.setVisible(true);
			int continueProcess = JOptionPane.showConfirmDialog(null, "Select Line To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
			if (continueProcess == JOptionPane.YES_OPTION) {
				imgFrame.setListenerState(ListenerState.SelectPath);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			File file = new File(OcharenaSettings.dataFolder + File.separator + "outputHough.jpg");
			ImageIO.write(pixels.getImageAsBufferedImage(), "jpg", file);
			invertImage(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		ImagePixels pxls = new ImagePixels(resultImage.getImage());
		System.out.println("I'm Here!");
		ColorUtils.printRGBValues(resultImage.getImage().getRGB(861, 66));
		//JophImgFrame tracedFrame = new JophImgFrame(resultImage.getImage());
		PannableImageFrame tracedFrame = new PannableImageFrame("Traced Image", resultImage.getImage());

		tracedFrame.setVisible(true);
	}

	public void houghTransform2(String imageFilename) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		ImagePixels pixels = new ImagePixels(imageFilename, metadata);
		pixels.prepareImage();
		try {
			File file = new File(OcharenaSettings.dataFolder + File.separator + "outputFiltered.jpg");
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
			File file = new File(OcharenaSettings.dataFolder + File.separator + "outputInverted.jpg");
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

		//CharTracer charTracer = new CharTracer(filename);
		//charTracer.basicTracing();
		//charTracer.histogramTracing();

		LineTracer lnTracer = new LineTracer(filename);
		BufferedImage img = null;
		try {
			img = lnTracer.histogramTracing();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		try {
			ImageIO.write(img, "png", new File(OcharenaSettings.dataFolder + File.separator + "output-" + System.currentTimeMillis() + ".png"));
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
