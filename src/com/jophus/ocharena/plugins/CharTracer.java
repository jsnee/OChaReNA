package com.jophus.ocharena.plugins;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import com.jophus.ocharena.document.LineSegmentedDocument;

public class CharTracer {

	private static final Logger LOG = Logger.getLogger(CharTracer.class.getName());
	
	private LineSegmentedDocument doc;
	
	public CharTracer(LineSegmentedDocument doc) {
		this.doc = doc;
	}
	
	public BufferedImage testTraceChars() {
		LOG.entering(this.getClass().toString(), "testTraceChars()");
		
		BufferedImage bImg;
		
		return new BufferedImage(0, 0, 0);
	}
	
	public void segmentChars() {
		
	}
	
}
