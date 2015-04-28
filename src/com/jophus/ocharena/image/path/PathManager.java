package com.jophus.ocharena.image.path;

import java.awt.Graphics;
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
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
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
	
	public PixelPath remove(int index) {
		return paths.remove(index);
	}

	public void setPathToFrame(int index, int lineWeight) {
		if (paths.get(index).getClass().getName().equals(RectPixelPath.class.getName()))
			((RectPixelPath)paths.get(index)).makePathFrame(lineWeight);
	}

	public void drawPaths(Graphics g) {
		for (PixelPath eachPath : paths) eachPath.draw(g);
	}
	
	public void setAllOutlineThickness(int outlineThickness) {
		for (PixelPath eachPath : paths) eachPath.setOutlineThickness(outlineThickness);
	}
	
	public int size() {
		return paths.size();
	}
	
	public PixelPath setPath(int index, PixelPath pixelPath) {
		return paths.set(index, pixelPath);
	}
	
	public void printToConsole() {
		for (PixelPath each : paths) {
			System.out.println("PixelPath at: [" + each.x + ", " + each.y + "], [" + (each.x + each.getBounds().width) + ", " + (each.y + each.getBounds().height) + "]");
		}
	}
}
