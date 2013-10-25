package com.jophus.ocharena.scanner;

import java.util.ArrayList;

import com.jophus.ocharena.image.ImagePixels;

public class rmsSegmentationListener implements LineSegmentationListener {
	
	private int minVal;
	private int maxVal;
	private int threshold = 128;
	private int rms;
	private int[] rows;
	private int rowCount = 0;

	@Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public void beginImage(ImagePixels imagePixels, int x1, int y1, int x2,
			int y2) {
		rows = new int[y2 - y1];
		this.minVal = x2 - x1;
		this.maxVal = 0;
		for (int row = y1; row < y2; row++) {
			int count = 0;
			for (int cell = x1; cell < x2; cell++) {
				if (imagePixels.getPixel(cell, row) < this.threshold) {
					count++;
				}
			}
			if (this.minVal > count) this.minVal = count;
			if (this.maxVal < count) this.maxVal = count;
		}
		rms = (int) Math.sqrt((double)((this.minVal * this.minVal + this.maxVal * this.maxVal) / 2));

	}

	@Override
	public void processCellRow(ImagePixels imagePixels, int x1, int x2, int index) {
		

		int count = 0;
		for (int cell = x1; cell < x2; cell++) {
			if (imagePixels.getPixel(cell, index) < this.threshold) {
				count++;
			}
		}
		
		if (count >= this.rms) {
			this.rows[index] = this.rowCount;
		} else {
			this.rows[index] = -1;
			if (index != 0 && rows[index - 1] != -1) {
				this.rowCount++;
			}
		}
	}

	@Override
	public void processRowMerge(ImagePixels imagePixels, int y1, int y2) {

	}

	@Override
	public int[] getRows() {
		return rows;
	}

}
