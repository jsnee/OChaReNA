package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.utils.gui.JophFileSelect;
import com.jophus.utils.gui.JophPanelDescription;

/**
 * ImageProcessPanel class. Graphical user interface for the program.
 * @author Joe Snee
 *
 */
public class ImageProcessPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final GuiController guiController;
	// Custom file selector
	private JophFileSelect fileSelect;
	private JPanel centerPanel;

	// Default file if no file selected - FOR TESTING PURPOSES ONLY
	private String defaultSelectedFile = OcharenaSettings.dataFolder + File.separator + "SCAN0001_cropped.jpg";

	public ImageProcessPanel(GuiController guiController) {
		this.guiController = guiController;

		this.setLayout(new BorderLayout());

		String desc = "This feature will isolate each line within an image and trace them saving the output.";
		this.add(new JophPanelDescription(this.getBackground(), desc), BorderLayout.NORTH);

		centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());

		fileSelect = new JophFileSelect();
		centerPanel.add(fileSelect);

		// Unfinished unguided document generation
		JButton btn = new JButton("Process Image");
		btn.addActionListener(getSubmitAction());
		centerPanel.add(btn);

		// User assisted image processing
		JButton guidedBtn = new JButton("Guided Image Processing");
		guidedBtn.addActionListener(getSubmitGuidedAction());
		centerPanel.add(guidedBtn);


		this.add(centerPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());

		// Generate histogram overlay to represent line segmentation - For documentation
		JButton histOverlay = new JButton("Generate Histogram Overlay");
		histOverlay.addActionListener(getHistOverlayAction());
		southPanel.add(histOverlay);

		this.add(southPanel, BorderLayout.SOUTH);
	}

	/**
	 * Convert PDF document into an image for processing
	 * @return the filepath of the converted jpeg image
	 */
	private String convertFromPdf() {
		String result = "";
		if (!(new File(fileSelect.getFilePath())).exists()) {
			// Make sure file exists
			JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
		} else {
			File tempImageFile;
			try {
				// Create a temporary file - image will be archived anyways
				tempImageFile = File.createTempFile(fileSelect.getFilePath(), ".jpeg");
				tempImageFile.deleteOnExit();
				ImagePixels image = new ImagePixels(fileSelect.getFilePath());
				ImageIO.write(image.getImageAsBufferedImage(), "jpg", tempImageFile);
				result = tempImageFile.getAbsolutePath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Get user guided processing button action listener
	 * @return the button's action listener
	 */
	private ActionListener getSubmitGuidedAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!(new File(fileSelect.getFilePath())).exists()) {
					JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
				} else {
					String selectedFile = fileSelect.getFilePath();
					if (selectedFile.endsWith(".pdf") || selectedFile.endsWith(".PDF")) {
						selectedFile = convertFromPdf();
					}
					if (selectedFile.endsWith(OcharenaSettings.OCH_FILE_EXTENSION)) {
						guiController.loadOchDocument(selectedFile);
						guiController.processLinesGuided();
					} else {
						guiController.processImageGuided(selectedFile);
					}
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}

		};
	}

	/**
	 * Get user unguided processing button action listener
	 * @return the button's action listener
	 */
	private ActionListener getSubmitAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!(new File(fileSelect.getFilePath())).exists()) {
					JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
				} else {
					String selectedFile = fileSelect.getFilePath();
					if (selectedFile.endsWith(".pdf") || selectedFile.endsWith(".PDF")) {
						selectedFile = convertFromPdf();
					}
					JOptionPane.showMessageDialog(null, "This Feature hasn't been finished yet!");
					//guiController.processImage(selectedFile);
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}

		};
	}


	/**
	 * Get histogram overlay button action listener
	 * @return the button's action listener
	 */
	private ActionListener getHistOverlayAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!(new File(fileSelect.getFilePath())).exists()) {
					JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
				} else {
					String selectedFile = fileSelect.getFilePath();
					if (selectedFile.endsWith(".pdf") || selectedFile.endsWith(".PDF")) {
						selectedFile = convertFromPdf();
						System.out.println(selectedFile);
					}
					guiController.generateHistogramOverlay(selectedFile);

					/* Used for generating images for capstone project */
					//guiController.houghTransform(selectedFile);
					//guiController.sobelEdge(selectedFile);
					//guiController.traceLinesTest(selectedFile);
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}

		};
	}
}
