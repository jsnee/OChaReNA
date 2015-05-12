package com.jophus.ocharena.image;

import java.util.ArrayList;

import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.RectPixelPath;

/**
 * Container class for detected line paths.
 * @author Joe Snee
 *
 */
public class DetectedLines {
	private PathManager pathManager;
	
	/**
	 * Empty path manager constructor.
	 * @param imageWidth
	 * @param imageHeight
	 */
	public DetectedLines(int imageWidth, int imageHeight) {
		pathManager = new PathManager(imageWidth, imageHeight);
	}
	
	/**
	 * Constructor using 2D ArrayLists of Integers.
	 * @param imageWidth
	 * @param imageHeight
	 * @param isLine
	 */
	public DetectedLines(int imageWidth, int imageHeight, ArrayList<ArrayList<Integer>> isLine) {
		pathManager = new PathManager(imageWidth, imageHeight);
		addLineList(isLine);
	}
	
	/**
	 * Constructor using line-by-line boolean array
	 * @param imageWidth
	 * @param imageHeight
	 * @param isLine
	 */
	public DetectedLines(int imageWidth, int imageHeight, boolean[] isLine) {
		pathManager = new PathManager(imageWidth, imageHeight);
		addLineList(convertBooleanArrayToLineList(isLine));
	}
	
	/**
	 * Gets the pathManager
	 * @return
	 */
	public PathManager getPathManager() {
		return pathManager;
	}
	
	/**
	 * Converts boolean line-by-line array to 2D ArrayLists of Integers
	 * @param isLine
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> convertBooleanArrayToLineList(boolean[] isLine) {
		ArrayList<ArrayList<Integer>> lineList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> eachLine = null;
		for (int i = 0; i < isLine.length; i++) {
			if (isLine[i]) {
				// Start of a new line
				if (eachLine == null) eachLine = new ArrayList<Integer>();
				eachLine.add(i);
			} else if (eachLine != null) {
				// End of line; Add it
				lineList.add(eachLine);
				eachLine = null;
			}
		}

		if (eachLine != null) {
			lineList.add(eachLine);
			eachLine = null;
		}
		return lineList;
	}
	
	/**
	 * Adds the specified lines to the path manager
	 * @param isLine
	 */
	private void addLineList(ArrayList<ArrayList<Integer>> isLine) {
		for (ArrayList<Integer> eachLine : isLine) {
			pathManager.addPath(new RectPixelPath(0, eachLine.get(0), pathManager.getWidth(), eachLine.get(eachLine.size() - 1) - eachLine.get(0)));
		}
	}
}
