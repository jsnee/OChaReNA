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

import com.jophus.houghtransformation.EHTProcessStep;
import com.jophus.houghtransformation.HTEngine;
import com.jophus.houghtransformation.HTImage;
import com.jophus.ocharena.Ocharena;
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
import com.jophus.util.InteractiveImageFrame;
import com.jophus.util.InteractiveImageFrame.ListenerState;
import com.jophus.utils.gui.JophImgFrame;

/**
 * GuiController class. Handles most of the process, start to finish
 * @author Joe Snee
 *
 */
public class GuiController {

	private static final Logger LOG = Logger.getLogger(GuiController.class.getName());

	private OCRMainFrame mainFrame;
	// Deprecated
	private ScannedDocument doc;
	private OCHDocument ochDoc;

	/**
	 * Default Constructor.
	 */
	public GuiController() {
		setLookAndFeel();
		mainFrame = new OCRMainFrame(this);
	}

	/**
	 * Setup the Gui
	 */
	public void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Load an OCHArchive by filename
	 * @param ochDocumentFilename the archive filename
	 */
	public void loadOchDocument(String ochDocumentFilename) {
		ochDoc = new OCHDocument(ochDocumentFilename);
	}

	/**
	 * Process the image unguided and generate a text document representation. Never fully implemented
	 * @param imageFilename the image filename
	 */
	public void processImage(String imageFilename) {
		LOG.entering(this.getClass().toString(), "processImage()");

		ochDoc = new OCHDocument(imageFilename);
		BasicLineTracer lineTracer = new BasicLineTracer(imageFilename);
		lineTracer.extractLines(ochDoc);
		JOptionPane.showMessageDialog(null, "Done!");
	}

	/**
	 * Begin processing the image, prompting user input where necessary.
	 * @param imageFilename the image filename
	 */
	public void processImageGuided(String imageFilename) {
		LOG.entering(this.getClass().toString(), "processImageGuided()");

		// Initialize the document and setup the line tracer
		ochDoc = new OCHDocument(imageFilename);
		BasicLineTracer lineTracer = new BasicLineTracer(imageFilename);
		try {
			// Display the document to the user, highlighting the detected lines 
			BufferedImage bimg = ImageIO.read(new File(imageFilename));
			InteractiveImageFrame imgFrame = new InteractiveImageFrame("Line Selection", bimg);
			imgFrame.setGuiController(this);
			long start = System.currentTimeMillis();
			// Detect the lines and set them in the Image Frame
			imgFrame.setDetectedLines(new DetectedLines(bimg.getWidth(), bimg.getHeight(), lineTracer.detectLines(ochDoc)));
			
			// Benchmarking stats
			long elapsed = System.currentTimeMillis() - start;
			LOG.info("Line Histogram Elapsed Processing Time: " + elapsed / 1000 + " seconds");
			
			imgFrame.setDisplayPathType(PathManagerType.DetectedLines);
			imgFrame.setVisible(true);
			// Prompt user for continuation
			int continueProcess = JOptionPane.showConfirmDialog(null, "Select Line To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
			if (continueProcess == JOptionPane.YES_OPTION) {
				imgFrame.setListenerState(ListenerState.SelectPath);
			} else {
				// Save for later, never implemented
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes the detected lines to isolate the individual characters
	 */
	public void processLinesGuided() {
		// Initialize character tracer
		BasicCharTracer charTracer = new BasicCharTracer(ochDoc);
		// Detect the characters
		PathManager charPaths = charTracer.detectChars();
		BufferedImage bimg = ochDoc.getImagePixels().getImageAsBufferedImage();
		
		// Display document with characters outlined
		InteractiveImageFrame imgFrame = new InteractiveImageFrame("Character Selection", bimg);
		imgFrame.setGuiController(this);
		imgFrame.setDetectedChars(charPaths);
		imgFrame.setDisplayPathType(PathManagerType.DetectedCharacters);
		imgFrame.setVisible(true);
		// Prompt user for continuation
		int continueProcess = JOptionPane.showConfirmDialog(null, "Select Char To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
		if (continueProcess == JOptionPane.YES_OPTION) {
			imgFrame.setListenerState(ListenerState.SelectPath);
		} else {
			// Not fully implemented yet
			guessChars(charPaths);
		}
	}

	// Unimplemented
	public void saveCharPaths(PathManager pathManager) {
	}

	/**
	 * Save the line images to the archive and prompt user for continuation
	 * @param linePaths
	 */
	public void archiveLines(PathManager linePaths) {
		// Build the directory to temporarily store the lines until they are archived - no longer needed
		//File lineDir = new File(OcharenaSettings.dataFolder + "lines" + File.separator);
		//lineDir.mkdir();
		try {
			// Create the line header file
			File lineSegmentHeaderFile = File.createTempFile("lines", OcharenaSettings.OCH_HEADER_EXTENSION);
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(lineSegmentHeaderFile));
			os.writeInt(linePaths.size());
			// Write each PixelPath to the line header file
			for (int i = 0; i < linePaths.size(); i++) {
				os.writeChar(OCHFile.NEWLINE_CHAR);
				os.writeChar(OCHFile.HYPHEN_CHAR);
				os.writeChar(linePaths.getPath(i).getSerialFlag().getCharCode());
				os.writeChar(OCHFile.SPACE_CHAR);
				linePaths.getPath(i).writePixelPath(os);
				/*
				ImagePixels lineImage = ochDoc.getImagePixels().getRowsAsSubimage(linePaths.getPath(i).getYIndicies());
				File lineFile = new File(OcharenaSettings.dataFolder + "lines" + File.separator + "line-" + i + ".jpg");
				ImageIO.write(lineImage.getImageAsBufferedImage(), "jpg", lineFile);
				*/
			}
			os.close();
			//ochDoc.archiveLines(lineDir);
			// Save the line header to the och archive
			ochDoc.archiveLineHeader(lineSegmentHeaderFile);
			lineSegmentHeaderFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		for (String eachFile : lineDir.list()) {
			File currentFile = new File(lineDir.getPath(), eachFile);
			currentFile.delete();
		}
		lineDir.delete();
		*/
		BasicCharTracer charTracer = new BasicCharTracer(ochDoc);
		// Dialog to specify training or not, NOT FULLY IMPLEMENTED YET
		Object[] options = { "Guided Training", "Autodetect", "Autodetect From Now On" };
		int continueProcess = JOptionPane.showOptionDialog(null, "How do you want to detect the characters?", "Character Processing", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		if (continueProcess == JOptionPane.YES_OPTION) { // Guided Training - unimplemented
			processLinesGuided();
		} else if (continueProcess == JOptionPane.NO_OPTION) { // Autodetect
			JOptionPane.showMessageDialog(null, "This feature hasn't been implemented!");
		} else if (continueProcess == JOptionPane.CANCEL_OPTION) { // Autodetect permanently
			JOptionPane.showMessageDialog(null, "This feature hasn't been implemented!");
		}
		JOptionPane.showMessageDialog(null, "Done!");
	}

	@Deprecated
	/**
	 * Archives individual character images
	 * @param charPaths
	 */
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

	/**
	 * Process characters and output guess
	 * @param charPaths
	 */
	public void guessChars(PathManager charPaths) {
		String guesses = "";
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
			Ocharena.neuralNet.setInput(dataRow.getData());
			Ocharena.neuralNet.calculate();
			double[] output = Ocharena.neuralNet.getOutput();
			System.out.println("Output: " + Arrays.toString(output));
			int bestGuess = 0;
			for (int j = 1; j < output.length; j++) {
				if (output[j] > output[bestGuess]) bestGuess = j;
			}
			if (output[bestGuess] > 0.9d) {
				System.out.println("Best guess for " + i + " at " + output[bestGuess] * 100 + "% confidence: " + OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess));
				guesses += OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess);
			} else {
				// Not very confident about this character
				guesses += OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess);
				//				guesses += "_";
			}
		}
		JOptionPane.showMessageDialog(null, "Here is my guess:\n" + guesses);
		System.out.println("Here is my guess: \n" + guesses);
	}

	/**
	 * Generate a histogram overlay. For presentation purposes.
	 * @param imageFilename
	 */
	public void generateHistogramOverlay(String imageFilename) {
		LOG.entering(this.getClass().toString(), "generateHistogramOverlay(String imageFilename)");

		ochDoc = new OCHDocument(imageFilename, false);
		LineTracer lineTracer = new LineTracer(imageFilename);
		try {
			ImageIO.write(lineTracer.generateHistogramOverlay(), "jpg", new File(OcharenaSettings.dataFolder + "histogramOverlay.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	/**
	 * Perform a Hough Transformation to detect lines
	 * @param imageFilename
	 */
	public void houghTransform(String imageFilename) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		metadata.setWritingColor(Color.black);
		metadata.setUniformBackgroundColor(false);
		ImagePixels pixels = new ImagePixels(imageFilename, metadata);
		HTEngine htEngine = new HTEngine();
		htEngine.setSourceImage(pixels.getImageAsBufferedImage());
		ArrayList<EHTProcessStep> steps = new ArrayList<EHTProcessStep>();
		steps.add(EHTProcessStep.STEP_GRAYSCALE);
		steps.add(EHTProcessStep.STEP_EDGE_DETECTION);
		steps.add(EHTProcessStep.STEP_EDGE_TRESHOLD);
		steps.add(EHTProcessStep.STEP_HOUGH_SPACE_TOP);
		steps.add(EHTProcessStep.STEP_HOUGH_SPACE_CENTER);
		steps.add(EHTProcessStep.STEP_HOUGH_SPACE_FILTERED);
		steps.add(EHTProcessStep.STEP_ORIGINAL_LINES_OVERLAYED);
		HTImage resultImage = htEngine.getHTProcessSteps(steps);
		ImagePixels pix = new ImagePixels(resultImage.getImage());
		try {
			File file = new File(OcharenaSettings.dataFolder + File.separator + "outputHough.jpg");
			ImageIO.write(pix.getImageAsBufferedImage(), "jpg", file);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		ImagePixels pxls = new ImagePixels(resultImage.getImage());
		//System.out.println("I'm Here!");
		ColorUtils.printRGBValues(resultImage.getImage().getRGB(861, 66));
		//JophImgFrame tracedFrame = new JophImgFrame(resultImage.getImage());
		InteractiveImageFrame tracedFrame = new InteractiveImageFrame("Traced Image", resultImage.getImage());

		tracedFrame.setVisible(true);
	}

	/**
	 * Perform a Sobel edge detection. For presentation purposes.
	 * @param imageFilename
	 */
	public void sobelEdge(String imageFilename) {
		DocumentMetadata metadata = new DocumentMetadata();
		metadata.setBlueLinedPaper(true);
		metadata.setRedMarginPaper(true);
		ImagePixels pixels = new ImagePixels(imageFilename, metadata);
		//		pixels.prepareImage();
		HTEngine htEngine = new HTEngine();
		htEngine.setSourceImage(pixels.getImageAsBufferedImage());
		ArrayList<EHTProcessStep> steps = new ArrayList<EHTProcessStep>();
		steps.add(EHTProcessStep.STEP_GRAYSCALE);
		steps.add(EHTProcessStep.STEP_EDGE_DETECTION);
		//steps.add(EHTProcessStep.STEP_EDGE_TRESHOLD);
		HTImage resultImage = htEngine.getHTProcessSteps(steps);
		ImagePixels pix = new ImagePixels(resultImage.getImage());
		pix.invertColors();
		try {
			File file = new File(OcharenaSettings.dataFolder + File.separator + "outputSobel.jpg");
			ImageIO.write(pix.getImageAsBufferedImage(), "png", file);
			traceLines(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}

		//JophImgFrame tracedFrame = new JophImgFrame(pixels.getImageAsBufferedImage());

		//tracedFrame.showFrame();
	}

	/**
	 * Makes the frame visible
	 */
	public void showGUI() {
		mainFrame.setVisible(true);
	}

	@Deprecated
	// Never implemented
	public void traceChars(File imageFile) {

	}

	@Deprecated
	public void traceCharTest(LineSegmentedDocument lsDoc) {
		LOG.entering(this.getClass().toString(), "traceCharTest()");

		CharTracer ct = new CharTracer(lsDoc);
	}

	@Deprecated
	public void traceLinesTest(String filename) {
		LOG.entering(this.getClass().toString(), "traceLinesTest()");

		doc = new ScannedDocument(filename);
		LineTracer lt = new LineTracer(filename);
		//LineSegmentedDocument lsDoc = lt.getSegmentedDoc(doc);
		//lsDoc.archiveLineImgs();
//		BufferedImage img = null;
//		try {
//			img = lt.getTracedImage();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			BufferedImage bimg = ImageIO.read(new File(filename));
			InteractiveImageFrame imgFrame = new InteractiveImageFrame("Line Selection", bimg);
			imgFrame.setGuiController(this);
			long start = System.currentTimeMillis();
			imgFrame.setDetectedLines(new DetectedLines(bimg.getWidth(), bimg.getHeight(), lt.segmentLines()));
			long elapsed = System.currentTimeMillis() - start;
			LOG.info("RMS Elapsed Processing Time: " + elapsed / 1000 + " seconds");
			imgFrame.setDisplayPathType(PathManagerType.DetectedLines);
			imgFrame.setVisible(true);
			int continueProcess = JOptionPane.showConfirmDialog(null, "Select Line To Segment?", "Continue Processing", JOptionPane.YES_NO_OPTION);
			if (continueProcess == JOptionPane.YES_OPTION) {
				imgFrame.setListenerState(ListenerState.SelectPath);
			} else {

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//		JophImgFrame tracedFrame = new JophImgFrame(img);
		//		tracedFrame.showFrame();

		//		try {
		//			ImageIO.write(lt.getTracedImage(), "jpg", new File(OcharenaSettings.dataFolder + "RMSTracedLines.jpg"));
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		LOG.info("Done!");
		LOG.exiting(this.getClass().toString(), "traceLinesTest()");
	}

	@Deprecated
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
