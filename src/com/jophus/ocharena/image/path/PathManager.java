package com.jophus.ocharena.image.path;

import java.awt.Rectangle;
import java.util.ArrayList;

public class PathManager {

	private ArrayList<PixelPath> paths;
	private int width;
	private int height;

	public PathManager(int width, int height) {
		paths = new ArrayList<PixelPath>();
		this.width = width;
		this.height = height;
	}

	public void addPath(PixelPath path) {
		if ((new Rectangle(0, 0, this.width, this.height)).contains(path.getBounds())) paths.add(path);
	}

	public boolean isPixelPathed(int x, int y) {
		for (PixelPath each : paths) {
			if (each.isPixelPathed(x, y)) return true;
		}
		return false;
	}

	public int getPathIndex(int x, int y) {
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).isPixelPathed(x, y)) return i;
		}
		return -1;
	}

	public PixelPath getPath(int index) {
		return paths.get(index);
	}

	public void setPathToFrame(int index, int lineWeight) {
		if (paths.get(index).getClass().getName().equals(RectPixelPath.class.getName()))
			((RectPixelPath)paths.get(index)).makePathFrame(lineWeight);
	}

}
