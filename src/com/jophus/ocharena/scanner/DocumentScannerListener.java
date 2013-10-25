package com.jophus.ocharena.scanner;

import com.jophus.ocharena.image.ImagePixels;

public interface DocumentScannerListener {

	public void beginDocument(ImagePixels imagePixels);
	
	public void beginRow(ImagePixels imagePixels, int y1, int y2);
	
	public void processChar(ImagePixels imagePixels, int x1, int y1, int x2, int y2, int rowY1, int rowY2);
	
	public void processSpace(ImagePixels imagePixels, int x1, int y1, int x2, int y2);
	
	public void endRow(ImagePixels imagePixels, int y1, int y2);
	
	public void endDocument(ImagePixels imagePixels);
}
