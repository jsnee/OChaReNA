package com.jophus.ocharena.image.path;

import java.util.EnumMap;

import com.jophus.ocharena.image.path.PathManagerStack.PathManagerType;

/**
 * PathManagerStack class. Handles multiple PathManagers (ie. one for detected lines, one for detected characters, etc.)
 * @author Joe Snee
 *
 */
public class PathManagerStack extends EnumMap<PathManagerType, PathManager> {
	private static final long serialVersionUID = 1L;

	// Path type enum
	public enum PathManagerType { DetectedLines, TransformLine, UserInputLines, DetectedCharacters, TransformCharacter, UserInputCharacters }
	
	private int imageWidth;
	private int imageHeight;
	
	/**
	 * Default Constructor
	 */
	public PathManagerStack() {
		super(new EnumMap<PathManagerType, PathManager>(PathManagerType.class));
	}
	
	/**
	 * Constructor. Defines image dimensions.
	 * @param imageWidth
	 * @param imageHeight
	 */
	public PathManagerStack(int imageWidth, int imageHeight) {
		super(new EnumMap<PathManagerType, PathManager>(PathManagerType.class));
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
	
	public int getWidth() {
		return imageWidth;
	}
	
	public int getHeight() {
		return imageHeight;
	}
}
