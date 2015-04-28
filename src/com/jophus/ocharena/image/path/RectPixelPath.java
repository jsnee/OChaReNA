package com.jophus.ocharena.image.path;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.jophus.util.JophRectFrame;

public class RectPixelPath extends PixelPath {

	private Rectangle path;

	public RectPixelPath(int x, int y, int width, int height) {
		this.path = new Rectangle(x, y, width, height);
		this.x = x;
		this.y = y;
	}
	
	public RectPixelPath(Rectangle bounds) {
		this.x = bounds.x;
		this.y = bounds.y;
		this.path = new Rectangle(x, y, bounds.width, bounds.height);
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

	@Override
	public void translate(int dx, int dy) {
		path.translate(dx, dy);
		x = path.x;
		y = path.y;
	}

	public void makePathFrame(int lineWeight) {
		path = new JophRectFrame(path, lineWeight);
	}

	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public Polygon getPolygon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void draw(Graphics g) {
		Color originalColor = g.getColor();
		g.setColor(outlineColor);
		if (isSpace) g.setColor(spaceOutlineColor);
		for (int i = 0; i < outlineThickness; i++) {
			g.drawRect(x + i, y + i, path.width - 2 * i, path.height - 2 * i);
		}
		g.setColor(originalColor);
	}

	@Override
	public void setOutlineThickness(int outlineThickness) {
		this.outlineThickness = outlineThickness;
	}
	
	@Override
	public void setBounds(Rectangle bounds) {
		path = bounds;
		x = bounds.x;
		y = bounds.y;
	}

	@Override
	public int[] getYIndicies() {
		int[] result = new int[path.height];
		for (int i = 0; i < path.height; i++) result[i] = path.y + i;
		return result;
	}

	@Override
	public PixelPath clone() {
		return new RectPixelPath(path);
	}

	@Override
	public void writePixelPath(ObjectOutputStream out) throws IOException {
		out.writeObject(path);
	}

	@Override
	public SerialFlag getSerialFlag() {
		return SerialFlag.RectPP;
	}
}
