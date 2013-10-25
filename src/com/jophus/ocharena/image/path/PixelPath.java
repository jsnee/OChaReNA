package com.jophus.ocharena.image.path;

import java.awt.Rectangle;

public abstract class PixelPath {
	
	protected int x;
	protected int y;
	
	public PixelPath() {
	}
	
	public abstract boolean isPixelPathed(int x, int y);
	public abstract boolean pathSurroundsPixel(int x, int y);
	public abstract void translate(int x, int y);
	public abstract int size();
	public abstract Rectangle getBounds();
}
