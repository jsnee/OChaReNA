package com.jophus.ocharena.image.path;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Abstract PixelPath Class
 * @author Joe Snee
 *
 */
public abstract class PixelPath {
	
	// Coordinate position
	protected int x;
	protected int y;
	// Thickness and color of outline to render
	protected int outlineThickness = 1;
	protected Color outlineColor = new Color(255, 0, 255);
	// Outline color of a space character
	protected Color spaceOutlineColor = new Color(255, 255, 0);
	protected boolean isSpace = false;
	
	// Character flag for file serialization and loading saved PixelPaths
	public enum SerialFlag { 
		// Currently only create rectangular PixelPaths, eventually
		//		should have freeform polygon PixelPaths
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
	
	/**
	 * Default Constructor.
	 */
	public PixelPath() {
	}
	
	/**
	 * Duplicate the object
	 */
	public abstract PixelPath clone();
	/**
	 * Determine if the specified pixel is inside of the path
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract boolean isPixelPathed(int x, int y);
	@Deprecated
	/**
	 * Determine if the specified pixel is inside of the path frame completely
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract boolean pathSurroundsPixel(int x, int y);
	/**
	 * Translate the PixelPath
	 * @param x
	 * @param y
	 */
	public abstract void translate(int x, int y);
	/**
	 * Set the rectangular bounds of the PixelPath
	 * @param bounds
	 */
	public abstract void setBounds(Rectangle bounds);
	/**
	 * Gets the size of the PixelPath
	 * @return
	 */
	public abstract int size();
	/**
	 * Gets the rectangular bounds of the PixelPath
	 * @return
	 */
	public abstract Rectangle getBounds();
	/**
	 * Gets a polygonal representation of the PixelPath
	 * @return
	 */
	public abstract Polygon getPolygon();
	/**
	 * Render the PixelPath
	 * @param g the render graphics
	 */
	public abstract void draw(Graphics g);
	/**
	 * Set the thickness of the path outline to be rendered
	 * @param outlineThickness
	 */
	public abstract void setOutlineThickness(int outlineThickness);
	/**
	 * Get an array of the pixel row y-indicies
	 * @return
	 */
	public abstract int[] getYIndicies();
	/**
	 * Write the serialized PixelPath to the specified output stream
	 * @param out
	 * @throws IOException
	 */
	public abstract void writePixelPath(ObjectOutputStream out) throws IOException;
	/**
	 * Get the serialization flag for the specified PixelPath class
	 * @return
	 */
	public abstract SerialFlag getSerialFlag();
	
	public void setIsSpace(boolean isSpace) {
		this.isSpace = isSpace;
	}
	
	public boolean getIsSpace() {
		return isSpace;
	}
	
	/**
	 * Generate the appropriate PixelPath saved to a file
	 * @param serialFlag
	 * @param o
	 * @return
	 */
	public static PixelPath buildPixelPath(char serialFlag, Object o) {
		if (serialFlag == SerialFlag.RectPP.getChar()) {
			return new RectPixelPath((Rectangle) o);
		}
		return null;
	}
}
