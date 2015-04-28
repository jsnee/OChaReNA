package com.jophus.ocharena.image;

import java.util.ArrayList;

import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.RectPixelPath;

public class DetectedLines {
	private PathManager pathManager;
	
	public DetectedLines(int imageWidth, int imageHeight) {
		pathManager = new PathManager(imageWidth, imageHeight);
	}
	
	public DetectedLines(int imageWidth, int imageHeight, ArrayList<ArrayList<Integer>> isLine) {
		pathManager = new PathManager(imageWidth, imageHeight);
		addLineList(isLine);
	}
	
	public DetectedLines(int imageWidth, int imageHeight, boolean[] isLine) {
		pathManager = new PathManager(imageWidth, imageHeight);
		addLineList(convertBooleanArrayToLineList(isLine));
	}
	
	public PathManager getPathManager() {
		return pathManager;
	}
	
	public static ArrayList<ArrayList<Integer>> convertBooleanArrayToLineList(boolean[] isLine) {
		ArrayList<ArrayList<Integer>> lineList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> eachLine = null;
		for (int i = 0; i < isLine.length; i++) {
			if (isLine[i]) {
				if (eachLine == null) eachLine = new ArrayList<Integer>();
				eachLine.add(i);
			} else if (eachLine != null) {
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
	
	private void addLineList(ArrayList<ArrayList<Integer>> isLine) {
		for (ArrayList<Integer> eachLine : isLine) {
			pathManager.addPath(new RectPixelPath(0, eachLine.get(0), pathManager.getWidth(), eachLine.get(eachLine.size() - 1) - eachLine.get(0)));
		}
	}
}
