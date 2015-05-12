package com.jophus.util;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.jophus.ocharena.Ocharena;
import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.gui.GuiController;
import com.jophus.ocharena.image.DetectedChars;
import com.jophus.ocharena.image.DetectedLines;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PathManagerStack;
import com.jophus.ocharena.image.path.PathManagerStack.PathManagerType;
import com.jophus.ocharena.image.path.PixelPath;
import com.jophus.ocharena.nn.OchDataRow;

/**
 * InteractiveImageFrame class. Custom made to interact and
 * modify detected lines and characters.
 * @author Joe
 *
 */
public class InteractiveImageFrame extends JFrame implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
	private static final long serialVersionUID = 1L;

	public enum ListenerState { SelectPath, TransformPath }

	// Right click menu options
	private static final String[] MenuOptionText = { "Transform Path", "Remove Path", "Add Path After", "Test Network", "Retrain All Characters", "Guess Character", "Save Image" };
	// Pixel threshold to allow for dragging
	private static final int DraggingMargin = 2;

	private JPanel canvas;
	private BufferedImage image;
	private ImagePixels imagePixels;
	private JScrollPane scrollPane;
	private PathManagerStack pathManagerStack;
	private PathManagerType displayedType = null;
	private ListenerState listenerState = null;
	private int selectedPath = -1;
	private int previousSelectedPath = -1;
	private int mouseX;
	private int mouseY;
	private int draggingSide = -1;
	private Point prevPoint = new Point();

	private PixelPath unalteredPath;

	private GuiController guiController = null;

	/**
	 * Constructor.
	 * @param title The frame title.
	 * @param img The image to display.
	 */
	public InteractiveImageFrame(String title, BufferedImage img) {
		pathManagerStack = new PathManagerStack(img.getWidth(), img.getHeight());
		canvas = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
				if (displayedType != null) pathManagerStack.get(displayedType).drawPaths(g);
			}
		};
		image = img;
		imagePixels = new ImagePixels(image);
		canvas.add(new JLabel(""));
		canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		scrollPane = new JScrollPane(canvas);
		scrollPane.setAutoscrolls(true);
		scrollPane.addMouseListener(this);
		scrollPane.addMouseMotionListener(this);
		addKeyListener(this);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
	}

	public void setGuiController(GuiController guiController) {
		this.guiController = guiController;
	}

	public void setListenerState(ListenerState listenerState) {
		this.listenerState = listenerState;
	}

	public void setDetectedLines(DetectedLines detectedLines) {
		pathManagerStack.put(PathManagerType.DetectedLines, detectedLines.getPathManager());
	}

	public void setDetectedChars(DetectedChars detectedChars) {
		pathManagerStack.put(PathManagerType.DetectedCharacters, detectedChars.getPathManager());
	}

	public void setDetectedChars(PathManager detectedChars) {
		pathManagerStack.put(PathManagerType.DetectedCharacters, detectedChars);
	}

	public void setDisplayPathType(PathManagerType pathManagerType) {
		displayedType = (pathManagerStack.get(pathManagerType) == null ? null : pathManagerType);
	}

	@Override
	/**
	 * Handles double-clicking on a PixelPath
	 * Double-clicking on a PixelPath allows it to be edited
	 */
	public void mouseClicked(MouseEvent e) {
		if (listenerState == null) return;
		switch (listenerState) {
		case SelectPath:
			selectedPath = pathManagerStack.get(displayedType).getPathIndex(e.getX() + scrollPane.getHorizontalScrollBar().getValue(), e.getY() + scrollPane.getVerticalScrollBar().getValue());
			pathManagerStack.get(displayedType).setAllOutlineThickness(1);
			if (selectedPath == -1) break;
			pathManagerStack.get(displayedType).getPath(selectedPath).setOutlineThickness(3);
			if (e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1) {
				unalteredPath = pathManagerStack.get(displayedType).getPath(selectedPath).clone();
				PathManager transformManager = new PathManager(pathManagerStack.getWidth(), pathManagerStack.getHeight());
				transformManager.addPath(pathManagerStack.get(displayedType).getPath(selectedPath));
				if (displayedType == PathManagerType.DetectedLines) {
					pathManagerStack.put(PathManagerType.TransformLine, transformManager);
					displayedType = PathManagerType.TransformLine;
				} else if (displayedType == PathManagerType.DetectedCharacters) {
					pathManagerStack.put(PathManagerType.TransformCharacter, transformManager);
					displayedType = PathManagerType.TransformCharacter;
				}
				listenerState = ListenerState.TransformPath;
				previousSelectedPath = selectedPath;
				selectedPath = 0;
				Rectangle bounds = transformManager.getPath(0).getBounds();
				System.out.println("x: " + bounds.x + " y: " + bounds.y + " width: " + bounds.width + " height: " + bounds.height);
			}
			break;
		case TransformPath:
			break;
		}
		update(getGraphics());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	/**
	 * Allows dragging to edit PixelPath
	 */
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX() + scrollPane.getHorizontalScrollBar().getValue();
		mouseY = e.getY() + scrollPane.getVerticalScrollBar().getValue();
		if (listenerState == null) return;
		switch (listenerState) {
		case SelectPath:
			break;
		case TransformPath:
			PixelPath pixelPath = pathManagerStack.get(displayedType).getPath(selectedPath);
			if (mouseY < pixelPath.getBounds().y + DraggingMargin && mouseY > pixelPath.getBounds().y - DraggingMargin) {
				draggingSide = 0;
				prevPoint = new Point(mouseX, mouseY);
			} else if (mouseX < pixelPath.getBounds().x + pixelPath.getBounds().width + DraggingMargin && mouseX > pixelPath.getBounds().x + pixelPath.getBounds().width - DraggingMargin) {
				draggingSide = 1;
				prevPoint = new Point(mouseX, mouseY);
			} else if (mouseY < pixelPath.getBounds().y + pixelPath.getBounds().height + DraggingMargin && mouseY > pixelPath.getBounds().y + pixelPath.getBounds().height - DraggingMargin) {
				draggingSide = 2;
				prevPoint = new Point(mouseX, mouseY);
			} else if (mouseX < pixelPath.getBounds().x + DraggingMargin && mouseX > pixelPath.getBounds().x - DraggingMargin) {
				draggingSide = 3;
				prevPoint = new Point(mouseX, mouseY);
			} else if (mouseY < pixelPath.getBounds().y + pixelPath.getBounds().height && mouseY > pixelPath.getBounds().y) {
				draggingSide = 4;
				prevPoint = new Point(mouseX, mouseY);
			}
			break;
		}
	}

	@Override
	/**
	 * Right-click menu handler
	 */
	public void mouseReleased(MouseEvent e) {
		if (listenerState == null) return;
		switch (listenerState) {
		case SelectPath:
			int pathIndex = pathManagerStack.get(displayedType).getPathIndex(e.getX() + scrollPane.getHorizontalScrollBar().getValue(), e.getY() + scrollPane.getVerticalScrollBar().getValue());
			if (selectedPath == -1 || pathIndex != selectedPath) {
				if (e.isPopupTrigger()) getEmptyRightClickMenu().show(e.getComponent(), e.getX(), e.getY());
			}
			if (e.isPopupTrigger()) getRightClickPathMenu().show(e.getComponent(), e.getX(), e.getY());
			break;
		case TransformPath:
			draggingSide = -1;
			break;
		}
		update(getGraphics());
	}

	/**
	 * Gets right click menu if clicked inside a PixelPath
	 * @return
	 */
	private JPopupMenu getRightClickPathMenu() {
		JPopupMenu result = new JPopupMenu();
		JMenuItem transformMenuItem = new JMenuItem(MenuOptionText[0]);
		transformMenuItem.addActionListener(this);
		result.add(transformMenuItem);
		JMenuItem removeMenuItem = new JMenuItem(MenuOptionText[1]);
		removeMenuItem.addActionListener(this);
		result.add(removeMenuItem);
		JMenuItem addPathAfter = new JMenuItem(MenuOptionText[2]);
		addPathAfter.addActionListener(this);
		result.add(addPathAfter);
		if (displayedType == PathManagerType.DetectedCharacters) {
			JMenuItem trainMenuItem = new JMenuItem(MenuOptionText[3]);
			trainMenuItem.addActionListener(this);
			result.add(trainMenuItem);
			JMenuItem pageTrainMenuItem = new JMenuItem(MenuOptionText[4]);
			pageTrainMenuItem.addActionListener(this);
			result.add(pageTrainMenuItem);
			JMenuItem guessMenuItem = new JMenuItem(MenuOptionText[5]);
			guessMenuItem.addActionListener(this);
			result.add(guessMenuItem);
		}
		JMenuItem saveImage = new JMenuItem(MenuOptionText[6]);
		saveImage.addActionListener(this);
		result.add(saveImage);
		return result;
	}
	
	/**
	 * Gets right click menu if clicked outside a PixelPath
	 * @return
	 */
	private JPopupMenu getEmptyRightClickMenu() {
		JPopupMenu result = new JPopupMenu();
		JMenuItem saveImage = new JMenuItem(MenuOptionText[6]);
		saveImage.addActionListener(this);
		result.add(saveImage);
		return result;
	}

	@Override
	/**
	 * Handles dragging mouse to edit PixelPath
	 */
	public void mouseDragged(MouseEvent e) {
		if (listenerState == null) return;
		int mouseMoveX = e.getX() + scrollPane.getHorizontalScrollBar().getValue();
		int mouseMoveY = e.getY() + scrollPane.getVerticalScrollBar().getValue();
		switch (listenerState) {
		case SelectPath:
			break;
		case TransformPath:
			Rectangle bounds;
			//System.out.println("Dragging Mouse...");
			switch (draggingSide) {
			case 0:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.height -= mouseMoveY - prevPoint.y;
				bounds.y += mouseMoveY - prevPoint.y;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			case 1:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.width += mouseMoveX - prevPoint.x;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			case 2:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.height += mouseMoveY - prevPoint.y;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			case 3:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.width -= mouseMoveX - prevPoint.x;
				bounds.x += mouseMoveX - prevPoint.x;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			case 4:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.y += mouseMoveY - prevPoint.y;
				bounds.x += mouseMoveX - prevPoint.x;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			}
			break;
		}
	}

	@Override
	/**
	 * Changes mouse cursor when editing a PixelPath
	 */
	public void mouseMoved(MouseEvent e) {
		int mouseMoveX = e.getX() + scrollPane.getHorizontalScrollBar().getValue();
		int mouseMoveY = e.getY() + scrollPane.getVerticalScrollBar().getValue();
		if (listenerState == null) return;
		switch (listenerState) {
		case SelectPath:
			break;
		case TransformPath:
			PixelPath pixelPath = pathManagerStack.get(displayedType).getPath(selectedPath);
			if (mouseMoveY < pixelPath.getBounds().y + DraggingMargin && mouseMoveY > pixelPath.getBounds().y - DraggingMargin) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			} else if (mouseMoveY < pixelPath.getBounds().y + pixelPath.getBounds().height + DraggingMargin && mouseMoveY > pixelPath.getBounds().y + pixelPath.getBounds().height - DraggingMargin) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
			} else if (mouseMoveX < pixelPath.getBounds().x + DraggingMargin && mouseMoveX > pixelPath.getBounds().x - DraggingMargin) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			} else if (mouseMoveX < pixelPath.getBounds().x + pixelPath.getBounds().width + DraggingMargin && mouseMoveX > pixelPath.getBounds().x + pixelPath.getBounds().width - DraggingMargin) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			} else if (mouseMoveY < pixelPath.getBounds().y + pixelPath.getBounds().height && mouseMoveY > pixelPath.getBounds().y) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			break;
		}
	}

	@Override
	/**
	 * Handles keyboard input
	 */
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ENTER:
			if (listenerState == ListenerState.TransformPath) {
				if (displayedType == PathManagerType.TransformLine) {
					displayedType = PathManagerType.DetectedLines;
				} else if (displayedType == PathManagerType.TransformCharacter) {
					displayedType = PathManagerType.DetectedCharacters;
				}
				listenerState = ListenerState.SelectPath;
				selectedPath = previousSelectedPath;
				previousSelectedPath = -1;
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				unalteredPath = null;
				update(getGraphics());
			} else if (listenerState == ListenerState.SelectPath) {
				int continueProcess = 0;
				if (displayedType == PathManagerType.DetectedLines)
					continueProcess = JOptionPane.showConfirmDialog(null, "Are you done selecting the lines?", "Continue Processing", JOptionPane.YES_NO_OPTION);
				else if (displayedType == PathManagerType.DetectedCharacters)
					continueProcess = JOptionPane.showConfirmDialog(null, "Are you done selecting the characters?", "Continue Processing", JOptionPane.YES_NO_OPTION);
				if (continueProcess == JOptionPane.YES_OPTION) {
					if (displayedType == PathManagerType.DetectedLines)
						guiController.archiveLines(pathManagerStack.get(displayedType));
					else if (displayedType == PathManagerType.DetectedCharacters)
						guiController.guessChars(pathManagerStack.get(displayedType));
					this.dispose();
				}
			}
			break;
		case KeyEvent.VK_UP:
			if (listenerState == ListenerState.TransformPath) {
				pathManagerStack.get(displayedType).getPath(selectedPath).translate(0, -1);
				update(getGraphics());
			}
			break;
		case KeyEvent.VK_DOWN:
			if (listenerState == ListenerState.TransformPath) {
				pathManagerStack.get(displayedType).getPath(selectedPath).translate(0, 1);
				update(getGraphics());
			}
			break;
		case KeyEvent.VK_LEFT:
			if (listenerState == ListenerState.TransformPath) {
				pathManagerStack.get(displayedType).getPath(selectedPath).translate(-1, 0);
				update(getGraphics());
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (listenerState == ListenerState.TransformPath) {
				pathManagerStack.get(displayedType).getPath(selectedPath).translate(1, 0);
				update(getGraphics());
			}
			break;
		case KeyEvent.VK_DELETE:
			if (listenerState == ListenerState.SelectPath && selectedPath >= 0) {
				unalteredPath = pathManagerStack.get(displayedType).remove(selectedPath);
				selectedPath = -1;
				update(getGraphics());
			}
			break;
		case KeyEvent.VK_Z:
			if (KeyEvent.getModifiersExText(e.getModifiersEx()).equals("Ctrl")) {
				if (listenerState == ListenerState.SelectPath && unalteredPath != null) {
					pathManagerStack.get(displayedType).addPath(unalteredPath);
					unalteredPath = null;
					pathManagerStack.get(displayedType).setAllOutlineThickness(1);
					update(getGraphics());
				} else if (listenerState == ListenerState.TransformPath && unalteredPath != null) {
					pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(unalteredPath.getBounds());
					unalteredPath = null;
					update(getGraphics());
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	/**
	 * Handles Right-click menu options
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(MenuOptionText[0])) {
			unalteredPath = pathManagerStack.get(displayedType).getPath(selectedPath).clone();
			PathManager transformManager = new PathManager(pathManagerStack.getWidth(), pathManagerStack.getHeight());
			transformManager.addPath(pathManagerStack.get(displayedType).getPath(selectedPath));
			if (displayedType == PathManagerType.DetectedLines) {
				pathManagerStack.put(PathManagerType.TransformLine, transformManager);
				displayedType = PathManagerType.TransformLine;
			} else if (displayedType == PathManagerType.DetectedCharacters) {
				pathManagerStack.put(PathManagerType.TransformCharacter, transformManager);
				displayedType = PathManagerType.TransformCharacter;
			}
			listenerState = ListenerState.TransformPath;
			previousSelectedPath = selectedPath;
			selectedPath = 0;
		} else if (e.getActionCommand().equals(MenuOptionText[1])) {
			unalteredPath = pathManagerStack.get(displayedType).remove(selectedPath);
			selectedPath = -1;
		} else if (e.getActionCommand().equals(MenuOptionText[2])) {
			pathManagerStack.get(displayedType).addPathAfter(selectedPath);
			selectedPath++;
		} else if (e.getActionCommand().equals(MenuOptionText[3])) {
			String s = (String)JOptionPane.showInputDialog(null, "What Characters are there?", "Test Characters", JOptionPane.PLAIN_MESSAGE, null, null, null);
			int[][] confusionMatrix = new int[OcharenaSettings.SUPPORTED_CHARS.length()][OcharenaSettings.SUPPORTED_CHARS.length()];
			
			for (int i = 0; i < OcharenaSettings.SUPPORTED_CHARS.length(); i++)
				for (int j = 0; j < OcharenaSettings.SUPPORTED_CHARS.length(); j++)
					confusionMatrix[i][j] = 0;
			
			if (s.length() == pathManagerStack.get(displayedType).size()) {
				System.out.println("Testing Set Against Network...");
				for (int i = 0; i < s.length(); i++) {
					OchDataRow dataRow = new OchDataRow(imagePixels.getPixelsFromPixelPath(pathManagerStack.get(displayedType).getPath(i)));
					Ocharena.neuralNet.setInput(dataRow.getData());
					Ocharena.neuralNet.calculate();
					double[] output = Ocharena.neuralNet.getOutput();
					int bestGuess = 0;
					for (int j = 1; j < output.length; j++) {
						if (output[j] > output[bestGuess]) bestGuess = j;
					}
					confusionMatrix[OcharenaSettings.SUPPORTED_CHARS.indexOf(s.charAt(i))][bestGuess]++;
					System.out.println("Best guess at " + output[bestGuess] * 100 + "% confidence: " + OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess) + ", actual character is: " + s.charAt(i));
					//OchDataRow dataRow = new OchDataRow(imagePixels.getPixelsFromPixelPath(pathManagerStack.get(displayedType).getPath(i)), s.charAt(i));
					//Ocharena.trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
				}
				System.out.println("Confusion Matrix:");
				System.out.print("  ");
				for (char eachChar : OcharenaSettings.SUPPORTED_CHARS.toCharArray())
					System.out.print(" " + eachChar + " ");
				System.out.println();
				for (int i = 0; i < OcharenaSettings.SUPPORTED_CHARS.length(); i++) {
					System.out.print(OcharenaSettings.SUPPORTED_CHARS.charAt(i) + " ");
					for (int j = 0; j < OcharenaSettings.SUPPORTED_CHARS.length(); j++) {
						if (i == j) {
							System.out.print(">" + confusionMatrix[j][i] + "<");
						} else {
							System.out.print(" " + confusionMatrix[j][i] + " ");
						}
					}
					System.out.println();
				}
			}
		} else if (e.getActionCommand().equals(MenuOptionText[4])) {
			String s = (String)JOptionPane.showInputDialog(null, "What Characters are there?", "Train Characters", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (s.length() == pathManagerStack.get(displayedType).size()) {
				System.out.println("Building Training Set...");
				for (int i = 0; i < s.length(); i++) {
					OchDataRow dataRow = new OchDataRow(imagePixels.getPixelsFromPixelPath(pathManagerStack.get(displayedType).getPath(i)), s.charAt(i));
					Ocharena.trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
				}
				System.out.println("Training Neural Network...");
				Ocharena.neuralNet.learn(Ocharena.trainingSet);
				System.out.println("Saving Neural Network...");
				Ocharena.neuralNet.save(OcharenaSettings.neuralNetworkFile);
			}
			// TODO test character detection
		} else if (e.getActionCommand().equals(MenuOptionText[5])) {
			//String s = (String)JOptionPane.showInputDialog(null, "What Character is it?", "Guess Characters", JOptionPane.PLAIN_MESSAGE, null, null, null);
			OchDataRow dataRow = new OchDataRow(imagePixels.getPixelsFromPixelPath(pathManagerStack.get(displayedType).getPath(selectedPath)));
			Ocharena.neuralNet.setInput(dataRow.getData());
			Ocharena.neuralNet.calculate();
			double[] output = Ocharena.neuralNet.getOutput();
			System.out.println("Output: " + Arrays.toString(output));
			int bestGuess = 0;
			for (int j = 1; j < output.length; j++) {
				if (output[j] > output[bestGuess]) bestGuess = j;
			}
			System.out.println("Best guess at " + output[bestGuess] * 100 + "% confidence: " + OcharenaSettings.SUPPORTED_CHARS.charAt(bestGuess));
			//			double[] error = Ocharena.neuralNet.getLearningRule().
			//			Ocharena.trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
			// TODO test character detection
		} else if (e.getActionCommand().equals(MenuOptionText[6])) {
			String s = (String)JOptionPane.showInputDialog(null, "What should the file be called?", "Save Image", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (s.equals("")) return;
			s += (s.endsWith(".jpg") ? "" : ".jpg");
			File saveFile = new File(OcharenaSettings.dataFolder + s);
			while (saveFile.exists()) {
				s = (String)JOptionPane.showInputDialog(null, "That filename is already taken!\nWhat should the file be called?", "Save Image", JOptionPane.PLAIN_MESSAGE, null, null, null);
				if (s.equals("")) return;
				s += (s.endsWith(".jpg") ? "" : ".jpg");
				saveFile = new File(OcharenaSettings.dataFolder + s);
			}
			BufferedImage saveImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics graphics = saveImg.getGraphics();
			graphics.drawImage(image, 0, 0, null);
			if (displayedType != null) pathManagerStack.get(displayedType).drawPaths(graphics);
			try {
				ImageIO.write(saveImg, "jpg", saveFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JOptionPane.showMessageDialog(null, "Saved!");
			return;
		} else return;
		update(getGraphics());
	}

	@Override
	public void update(Graphics g) {
		Graphics offgc;
		Image offscreen = null;
		Dimension d = size();
		// create the offscreen buffer and associated Graphics
		offscreen = createImage(d.width, d.height);
		offgc = offscreen.getGraphics();
		// clear the exposed area
		offgc.setColor(getBackground());
		offgc.fillRect(0, 0, d.width, d.height);
		offgc.setColor(getForeground());
		// do normal redraw
		paint(offgc);
		// transfer offscreen to window
		g.drawImage(offscreen, 0, 0, this);
	}
}
