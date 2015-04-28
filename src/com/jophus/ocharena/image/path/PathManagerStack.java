package com.jophus.ocharena.image.path;

import java.util.EnumMap;

import com.jophus.ocharena.image.path.PathManagerStack.PathManagerType;

public class PathManagerStack extends EnumMap<PathManagerType, PathManager> {
	private static final long serialVersionUID = 1L;

	public enum PathManagerType { DetectedLines, TransformLine, UserInputLines, DetectedCharacters, TransformCharacter, UserInputCharacters }
	
	private int imageWidth;
	private int imageHeight;
	
	public PathManagerStack() {
		super(new EnumMap<PathManagerType, PathManager>(PathManagerType.class));
	}
	
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
