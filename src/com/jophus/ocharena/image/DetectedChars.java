package com.jophus.ocharena.image;

import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PixelPath;

public class DetectedChars {

	private PathManager pathManager;
	
	public DetectedChars(int imageWidth, int imageHeight) {
		pathManager = new PathManager(imageWidth, imageHeight);
	}
	
	public PathManager getPathManager() {
		return pathManager;
	}
	
	public void addCharPath(PixelPath path) {
		pathManager.addPath(path);
	}
}
