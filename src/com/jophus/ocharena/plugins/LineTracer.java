package com.jophus.ocharena.plugins;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.scanner.DocumentScanner;
import com.jophus.ocharena.scanner.DocumentScannerListener;

public class LineTracer {
	
	private DocumentScanner documentScanner = new DocumentScanner();
	private BufferedImage bImage;
	private Graphics2D bImageGraphics;

	
	
	public BufferedImage getTracedImage(File imageFile) {
		return null;
	}

}
