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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.jophus.ocharena.Ocharena;
import com.jophus.ocharena.factory.OchDataFactory;
import com.jophus.ocharena.gui.GuiController;
import com.jophus.ocharena.image.DetectedChars;
import com.jophus.ocharena.image.DetectedLines;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PathManagerStack;
import com.jophus.ocharena.image.path.PathManagerStack.PathManagerType;
import com.jophus.ocharena.image.path.PixelPath;
import com.jophus.ocharena.nn.OchDataRow;

public class PannableImageFrame extends JFrame implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
	private static final long serialVersionUID = 1L;

	public enum ListenerState { SelectPath, TransformPath }

	private static final String[] MenuOptionText = { "Transform Path", "Remove Path", "Train Character", "Guess Character" };
	private static final int DraggingMargin = 2;

	private JPanel canvas;
	private BufferedImage image;
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

	public PannableImageFrame(String title, BufferedImage img) {
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
		canvas.add(new JLabel(""));
		canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		scrollPane = new JScrollPane(canvas);
		scrollPane.setAutoscrolls(true);
		scrollPane.addMouseListener(this);
		scrollPane.addMouseMotionListener(this);
		//scrollPane.addKeyListener(this);
		addKeyListener(this);
		/*
		scrollPane.getViewport().addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
		        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		        scrollPane.getViewport().scrollRectToVisible(r);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});
		 */
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
			} else if (mouseY < pixelPath.getBounds().y + pixelPath.getBounds().height + DraggingMargin && mouseY > pixelPath.getBounds().y + pixelPath.getBounds().height - DraggingMargin) {
				draggingSide = 2;
				prevPoint = new Point(mouseX, mouseY);
			} else if (mouseY < pixelPath.getBounds().y + pixelPath.getBounds().height && mouseY > pixelPath.getBounds().y) {
				draggingSide = 4;
				prevPoint = new Point(mouseX, mouseY);
			}
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (listenerState == null) return;
		switch (listenerState) {
		case SelectPath:
			int pathIndex = pathManagerStack.get(displayedType).getPathIndex(e.getX() + scrollPane.getHorizontalScrollBar().getValue(), e.getY() + scrollPane.getVerticalScrollBar().getValue());
			if (selectedPath == -1 || pathIndex != selectedPath) return;
			if (e.isPopupTrigger()) getRightClickPathMenu().show(e.getComponent(), e.getX(), e.getY());
			break;
		case TransformPath:
			draggingSide = -1;
			break;
		}
		update(getGraphics());
	}

	private JPopupMenu getRightClickPathMenu() {
		JPopupMenu result = new JPopupMenu();
		JMenuItem transformMenuItem = new JMenuItem(MenuOptionText[0]);
		transformMenuItem.addActionListener(this);
		result.add(transformMenuItem);
		JMenuItem removeMenuItem = new JMenuItem(MenuOptionText[1]);
		removeMenuItem.addActionListener(this);
		result.add(removeMenuItem);
		if (displayedType == PathManagerType.DetectedCharacters) {
			JMenuItem trainMenuItem = new JMenuItem(MenuOptionText[2]);
			trainMenuItem.addActionListener(this);
			result.add(trainMenuItem);
			JMenuItem guessMenuItem = new JMenuItem(MenuOptionText[3]);
			guessMenuItem.addActionListener(this);
			result.add(guessMenuItem);
		}
		return result;
	}

	@Override
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
				break;
			case 2:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.height += mouseMoveY - prevPoint.y;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			case 3:
				break;
			case 4:
				bounds = pathManagerStack.get(displayedType).getPath(selectedPath).getBounds();
				bounds.y += mouseMoveY - prevPoint.y;
				pathManagerStack.get(displayedType).getPath(selectedPath).setBounds(bounds);
				prevPoint = new Point(mouseMoveX, mouseMoveY);
				update(getGraphics());
				break;
			}
			break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//		int mouseMoveX = e.getX() + scrollPane.getHorizontalScrollBar().getValue();
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
			} else if (mouseMoveY < pixelPath.getBounds().y + pixelPath.getBounds().height && mouseMoveY > pixelPath.getBounds().y) {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			break;
		}
	}

	@Override
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
			String s = (String)JOptionPane.showInputDialog(null, "What Character is it?", "Train Character", JOptionPane.PLAIN_MESSAGE, null, null, null);
			OchDataRow dataRow = new OchDataRow(new ImagePixels(image).getPixelsFromPixelPath(pathManagerStack.get(displayedType).getPath(selectedPath)), s.charAt(0));
			Ocharena.trainingSet.addRow(dataRow.getData(), dataRow.getExpectedOutputArray());
		}  else if (e.getActionCommand().equals(MenuOptionText[3])) {
			// TODO test character detection
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
