package com.jophus.ocharena.image.path;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * PathManager class. Container class for multiple PixelPaths
 * @author Joe Snee
 *
 */
public class PathManager {

	private ArrayList<PixelPath> paths;
	// Image diemnsions
	private int width;
	private int height;

	/**
	 * Constructor. Sets image width and height
	 * @param width
	 * @param height
	 */
	public PathManager(int width, int height) {
		paths = new ArrayList<PixelPath>();
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the image width
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the image height
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Add a PixelPath to the manager
	 * @param path
	 */
	public void addPath(PixelPath path) {
		if ((new Rectangle(0, 0, this.width, this.height)).contains(path.getBounds())) paths.add(path);
	}

	/**
	 * Determines if the specified pixel is inside of any of the pixel paths
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return
	 */
	public boolean isPixelPathed(int x, int y) {
		for (PixelPath each : paths) {
			if (each.isPixelPathed(x, y)) return true;
		}
		return false;
	}

	/**
	 * Gets the PixelPath index that contains the specified pixel
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return the pixel path index; -1 if the pixel isn't inside a PixelPath
	 */
	public int getPathIndex(int x, int y) {
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).isPixelPathed(x, y)) return i;
		}
		return -1;
	}

	/**
	 * Gets the PixelPath at the given index
	 * @param index
	 * @return
	 */
	public PixelPath getPath(int index) {
		return paths.get(index);
	}

	/**
	 * Removes the specified PixelPath
	 * @param index
	 * @return the removed PixelPath
	 */
	public PixelPath remove(int index) {
		return paths.remove(index);
	}

	@Deprecated
	/**
	 * Sets the pixel path to be rendered as a box outline
	 * deprecated. See PixelPath.draw(Graphics g)
	 * @param index
	 * @param lineWeight
	 */
	public void setPathToFrame(int index, int lineWeight) {
		if (paths.get(index).getClass().getName().equals(RectPixelPath.class.getName()))
			((RectPixelPath)paths.get(index)).makePathFrame(lineWeight);
	}

	/**
	 * Draws all of the contained paths
	 * @param g the render graphics
	 */
	public void drawPaths(Graphics g) {
		for (PixelPath eachPath : paths) eachPath.draw(g);
	}

	/**
	 * Set the outline thickness for all of the paths
	 * @param outlineThickness
	 */
	public void setAllOutlineThickness(int outlineThickness) {
		for (PixelPath eachPath : paths) eachPath.setOutlineThickness(outlineThickness);
	}

	/**
	 * Returns the number of paths
	 * @return
	 */
	public int size() {
		return paths.size();
	}

	/**
	 * Sets the PixelPath at the specified index to be the specified PixelPath
	 * @param index
	 * @param pixelPath
	 * @return returns the PixelPath that was overridden (if any)
	 */
	public PixelPath setPath(int index, PixelPath pixelPath) {
		return paths.set(index, pixelPath);
	}

	/**
	 * Debugging use. Prints out PixelPath details to console
	 */
	public void printToConsole() {
		for (PixelPath each : paths) {
			System.out.println("PixelPath at: [" + each.x + ", " + each.y + "], [" + (each.x + each.getBounds().width) + ", " + (each.y + each.getBounds().height) + "]");
		}
	}

	/**
	 * Adds a new path after the specified path as a clone of the selected path
	 * @param selectedPath
	 */
	public void addPathAfter(int selectedPath) {
		// Not the best way to do this, paths could get out of order, which could lead to false training
		PixelPath toAdd = paths.get(selectedPath).clone();
		if (selectedPath + 1 < paths.size()) {
			paths.add(selectedPath + 1, toAdd);
		} else {
			paths.add(toAdd);
		}
	}
}
