package com.jophus.ocharena.scanner;

import java.util.ArrayList;

import com.jophus.ocharena.image.ImagePixels;

public class DocumentScanner {

	public final void scan(
			ImagePixels imagePixels,
			DocumentScannerListener listener,
			int chunkX1,
			int chunkY1,
			int chunkX2,
			int chunkY2) {

		if(chunkX1 < 0) {
			chunkX1 = 0;
		} else if(chunkX1 >= imagePixels.getWidth()) {
			chunkX1 = imagePixels.getWidth() - 1;
		}
		if(chunkY1 < 0) {
			chunkY1 = 0;
		} else if(chunkY1 >= imagePixels.getHeight()) {
			chunkY1 = imagePixels.getHeight() - 1;
		}
		if(chunkX2 <= 0 || chunkX2 <= imagePixels.getWidth()) chunkX2 = imagePixels.getWidth() - 1;
		if(chunkY2 <= 0 || chunkY2 <= imagePixels.getHeight()) chunkY2 = imagePixels.getHeight() - 1;
		
		chunkX2++;
		chunkY2++;
		
		int[] rows = this.lineSegmentation(imagePixels, new rmsSegmentationListener(), chunkX1, chunkY1, chunkX2, chunkY2);
		
		
		
		
	}
	
	public int[] lineSegmentation(
			ImagePixels imagePixels,
			LineSegmentationListener segmentationListener,
			int chunkX1,
			int chunkY1,
			int chunkX2,
			int chunkY2) {
		
		segmentationListener.beginImage(imagePixels, chunkX1, chunkY1, chunkX2, chunkY2);
		
		int[] rows = segmentationListener.getRows();
		
		return rows;
	}

}
