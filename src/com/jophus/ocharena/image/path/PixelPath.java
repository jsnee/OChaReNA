package com.jophus.ocharena.image.path;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class PixelPath {
	
	protected int x;
	protected int y;
	protected int outlineThickness = 1;
	protected Color outlineColor = new Color(255, 0, 255);
	protected Color spaceOutlineColor = new Color(255, 255, 0);
	protected boolean isSpace = false;
	
	public enum SerialFlag { 
		RectPP(82);
		
		private final int charCode;
		SerialFlag(int cc) {
			charCode = cc;
		}
		
		public char getChar() {
			return (char)charCode;
		}
		
		public int getCharCode() {
			return charCode;
		}
	}
	
	public PixelPath() {
	}
	
	public abstract PixelPath clone();
	public abstract boolean isPixelPathed(int x, int y);
	public abstract boolean pathSurroundsPixel(int x, int y);
	public abstract void translate(int x, int y);
	public abstract void setBounds(Rectangle bounds);
	public abstract int size();
	public abstract Rectangle getBounds();
	public abstract Polygon getPolygon();
	public abstract void draw(Graphics g);
	public abstract void setOutlineThickness(int outlineThickness);
	public abstract int[] getYIndicies();
	public abstract void writePixelPath(ObjectOutputStream out) throws IOException;
	public abstract SerialFlag getSerialFlag();
	
	public void setIsSpace(boolean isSpace) {
		this.isSpace = isSpace;
	}
	
	public boolean getIsSpace() {
		return isSpace;
	}
	
	public static PixelPath buildPixelPath(char serialFlag, Object o) {
		if (serialFlag == SerialFlag.RectPP.getChar()) {
			return new RectPixelPath((Rectangle) o);
		}
		return null;
	}
}
