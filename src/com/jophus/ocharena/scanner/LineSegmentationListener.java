package com.jophus.ocharena.scanner;

import java.util.ArrayList;

import com.jophus.ocharena.image.ImagePixels;

public interface LineSegmentationListener {
	
	public void setThreshold(int threshold);
	
	public void beginImage(ImagePixels imagePixels, int x1, int y1, int x2, int y2);
	
	public void processCellRow(ImagePixels imagePixels, int x1, int x2, int index);
	
	public void processRowMerge(ImagePixels imagePixels, int y1, int y2);
	
	public int[] getRows();

}
