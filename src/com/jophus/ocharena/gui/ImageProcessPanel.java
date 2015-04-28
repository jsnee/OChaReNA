package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.utils.gui.JophFileSelect;
import com.jophus.utils.gui.JophPanelDescription;

public class ImageProcessPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final GuiController guiController;
	private JophFileSelect fileSelect;
	private JPanel centerPanel;
	
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

		JButton btn = new JButton("Process Image");
		btn.addActionListener(getSubmitAction());
		centerPanel.add(btn);
		
		JButton guidedBtn = new JButton("Guided Image Processing");
		guidedBtn.addActionListener(getSubmitGuidedAction());
		centerPanel.add(guidedBtn);


		this.add(centerPanel, BorderLayout.CENTER);
	}

	private ActionListener getSubmitGuidedAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!(new File(fileSelect.getFilePath())).exists()) {
					//JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
//					guiController.processImageGuided(defaultSelectedFile);
					guiController.processImageGuided("G:\\Users\\Joe\\Dropbox\\OChaReNA\\Capstone\\OChaReNA\\data\\KaylaFont.jpg");
//					JOptionPane.showMessageDialog(null, "Done!");
				} else {
					String selectedFile = fileSelect.getFilePath();
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
		
		private ActionListener getSubmitAction() {
			return new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (!(new File(fileSelect.getFilePath())).exists()) {
						//JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
						guiController.processImage(defaultSelectedFile);
//						guiController.processImage("G:\\Users\\Joe\\Dropbox\\OChaReNA\\Capstone\\OChaReNA\\data\\KaylaFont.jpg");
					} else {
						String selectedFile = fileSelect.getFilePath();
						guiController.processImage(selectedFile);
						JOptionPane.showMessageDialog(null, "Done!");
					}
				}

			};
	}
}
