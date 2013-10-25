package com.jophus.ocharena.scanner;

import com.jophus.ocharena.image.ImagePixels;

public class DocumentScannerListenerAdaptor implements DocumentScannerListener {

	@Override
	public void beginDocument(ImagePixels imagePixels) {
	}

	@Override
	public void beginRow(ImagePixels imagePixels, int y1, int y2) {
	}

	@Override
	public void processChar(ImagePixels imagePixels, int x1, int y1, int x2,
			int y2, int rowY1, int rowY2) {
	}

	@Override
	public void processSpace(ImagePixels imagePixels, int x1, int y1, int x2,
			int y2) {
	}

	@Override
	public void endRow(ImagePixels imagePixels, int y1, int y2) {
	}

	@Override
	public void endDocument(ImagePixels imagePixels) {
	}

}
