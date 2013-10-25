package com.jophus.util;

import java.awt.Rectangle;

public class JophRectFrame extends Rectangle {

	private int lineWeight;
	
	public JophRectFrame(int x, int y, int width, int height, int lineWeight) {
		super(x, y, width, height);
		this.lineWeight = lineWeight;
	}
	
	public JophRectFrame(Rectangle rect, int lineWeight) {
		super(rect.x, rect.y, rect.width, rect.height);
		this.lineWeight = lineWeight;
	}
	
	public Rectangle getInnerFrame() {
		return new Rectangle(this.x + lineWeight, this.y + lineWeight, this.width - (2 * lineWeight), this.height - (2 * lineWeight));
	}
	
	public boolean isOnFrame(int x, int y) {
		if (this.contains(x, y) && !this.getInnerFrame().contains(x, y)) return true;
		return false;
	}
}
