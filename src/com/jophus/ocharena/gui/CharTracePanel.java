package com.jophus.ocharena.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jophus.utils.gui.JophFileSelect;
import com.jophus.utils.gui.JophPanelDescription;

public class CharTracePanel extends JPanel {

	private final GUIController guiController;
	private JophFileSelect fileSelect;
	private JPanel centerPanel;
	
	public CharTracePanel(GUIController guiController)
	{
		this.guiController = guiController;
		
		this.setLayout(new BorderLayout());
		
		String desc = "This feature will isolate characters within an image and trace them in the GUI.";
		this.add(new JophPanelDescription(this.getBackground(), desc), BorderLayout.NORTH);
		
		centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		
		fileSelect = new JophFileSelect();
		centerPanel.add(fileSelect);
		
		JButton btn = new JButton("Process Image");
		btn.addActionListener(getSubmitAction());
		centerPanel.add(btn);
		
		this.add(centerPanel, BorderLayout.CENTER);
	}
	
	private ActionListener getSubmitAction() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File imageFile = new File(fileSelect.getFilePath());
				
				if (!imageFile.exists()) {
					JOptionPane.showMessageDialog(null, "Source Image Does Not Exist!");
				} else {
					guiController.traceChars(imageFile);
					JOptionPane.showMessageDialog(null, "Done!");
				}
			}
			
		};
	}
	
	private static final Logger LOG = Logger.getLogger(CharTracePanel.class.getName());
}
