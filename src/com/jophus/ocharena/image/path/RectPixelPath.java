package com.jophus.ocharena.image.path;

import java.awt.Point;
import java.awt.Rectangle;

import com.jophus.util.JophRectFrame;

public class RectPixelPath extends PixelPath {
	
	private Rectangle path;
	
	public RectPixelPath(int x, int y, int width, int height) {
		this.path = new Rectangle(x, y, width, height);
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean isPixelPathed(int x, int y) {
		if (path.getClass().getName().equals(JophRectFrame.class.getName())) {
			return ((JophRectFrame)path).isOnFrame(x, y);
		}
		return path.contains(x, y);
	}

	@Override
	public boolean pathSurroundsPixel(int x, int y) {
		return path.contains(x, y);
	}

	@Override
	public int size() {
		return path.width * path.height;
	}
	
	public Point getLocation() {
		return path.getLocation();
	}
	
	public Point getCenter() {
		return new Point((int)path.getCenterX(), (int)path.getCenterY());
	}
	
	public void translate(int dx, int dy) {
		path.translate(dx, dy);
	}
	
	public void makePathFrame(int lineWeight) {
		path = new JophRectFrame(path, lineWeight);
	}

	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

}
