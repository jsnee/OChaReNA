package com.jophus.ocharena.image;

import java.awt.Color;

public class DocumentMetadata {

	private boolean isBlueLinedPaper = false;
	private boolean isRedMarginPaper = false;
	private Color writingColor = Color.black;
	private boolean uniformBackgroundColor = true;

	public DocumentMetadata() {
	}
	
	public boolean isUniformBackgroundColor() {
		return uniformBackgroundColor;
	}

	public void setUniformBackgroundColor(boolean uniformBackgroundColor) {
		this.uniformBackgroundColor = uniformBackgroundColor;
	}

	public boolean isBlueLinedPaper() {
		return isBlueLinedPaper;
	}

	public void setBlueLinedPaper(boolean isBlueLinedPaper) {
		this.isBlueLinedPaper = isBlueLinedPaper;
	}

	public boolean isRedMarginPaper() {
		return isRedMarginPaper;
	}

	public void setRedMarginPaper(boolean isRedMarginPaper) {
		this.isRedMarginPaper = isRedMarginPaper;
	}

	public Color getWritingColor() {
		return writingColor;
	}

	public void setWritingColor(Color writingColor) {
		this.writingColor = writingColor;
	}
}
