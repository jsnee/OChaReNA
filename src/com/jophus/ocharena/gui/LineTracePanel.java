package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jophus.houghtransformation.HTEngine;
import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.OCHDocument;
import com.jophus.utils.gui.JophFileSelect;
import com.jophus.utils.gui.JophPanelDescription;

public class LineTracePanel extends JPanel {
	private final GuiController guiController;
	private JophFileSelect fileSelect;
	private JPanel centerPanel;

	public LineTracePanel(GuiController guiController)
	{
		this.guiController = guiController;

		this.setLayout(new BorderLayout());

		String desc = "This feature will isolate each line within an image and trace them in the GUI.";
		this.add(new JophPanelDescription(this.getBackground(), desc), BorderLayout.NORTH);

		centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());

		fileSelect = new JophFileSelect();
		centerPanel.add(fileSelect);

		JButton btn = new JButton("Process Image");
		btn.addActionListener(getSubmitAction());
		centerPanel.add(btn);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		JButton test = new JButton("Run Diagnostic Tests");
		test.addActionListener(getTestAction());
		JButton houghBtn = new JButton("Run Hough Transformation");
		houghBtn.addActionListener(getHoughAction());
		southPanel.add(test);
		southPanel.add(houghBtn);
		this.add(southPanel, BorderLayout.SOUTH);


		this.add(centerPanel, BorderLayout.CENTER);
	}

	private ActionListener getSubmitAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!(new File(fileSelect.getFilePath())).exists()) {
					//JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
					guiController.traceLines(OcharenaSettings.dataFolder + File.separator + "SCAN0001_cropped.jpg");
					JOptionPane.showMessageDialog(null, "Done!");
				} else {
					//guiController.houghTransform2(fileSelect.getFilePath());
					String selectedFile = fileSelect.getFilePath();
					//guiController.houghTransform(selectedFile);
					OCHDocument ochDoc = new OCHDocument(selectedFile);
					guiController.traceLines(selectedFile);
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}

		};
	}

	private ActionListener getHoughAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!(new File(fileSelect.getFilePath())).exists()) {
					//JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
					//guiController.traceLinesTest("images" + File.separator + "test_case.jpg");
					String selectedFile = "G:\\Documents\\OChaReNA-data\\KaylaFont\\KaylaFont.jpg";
					guiController.houghTransform(selectedFile);
					JOptionPane.showMessageDialog(null, "Done!");
				} else {
					//guiController.traceLinesTest(fileSelect.getFilePath());
					String selectedFile = fileSelect.getFilePath();
					//guiController.houghTransform(selectedFile);
					OCHDocument ochDoc = new OCHDocument(selectedFile);
					guiController.traceLines(ochDoc.getFilepath());
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}
		};
	}

	private ActionListener getTestAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!(new File(fileSelect.getFilePath())).exists()) {
					//JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
					//guiController.traceLinesTest("images" + File.separator + "test_case.jpg");
					String selectedFile = "G:\\eclipse\\Workspaces\\Capstone\\OChaReNA\\outputHough.jpg";
					guiController.invertImage(selectedFile);
					JOptionPane.showMessageDialog(null, "Whoops!");
				} else {
					//guiController.traceLinesTest(fileSelect.getFilePath());
					String selectedFile = fileSelect.getFilePath();
					OCHDocument ochDoc = new OCHDocument(selectedFile);
					guiController.traceLines(ochDoc.getFilepath());
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}
		};
	}

	private static final Logger LOG = Logger.getLogger(LineTracePanel.class.getName());

}
