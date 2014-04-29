package com.jophus.ocharena.document;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

public class DocumentSegment {
	
	private Polygon segment;
	
	public DocumentSegment(Rectangle segment) {
		this.segment = new Polygon(new int[]{ segment.x, segment.x + segment.width, segment.x + segment.width, segment.x },
				new int[]{ segment.y, segment.y, segment.y + segment.width, segment.y + segment.width },
				4);
	}
	
	public void setSegment(Rectangle segment) {
		this.segment.reset();
		this.segment = new Polygon(new int[]{ segment.x, segment.x + segment.width, segment.x + segment.width, segment.x },
				new int[]{ segment.y, segment.y, segment.y + segment.width, segment.y + segment.width },
				4);
	}
	
	public Rectangle getBounds() {
		return segment.getBounds();
	}
	
	public boolean contains(int x, int y) {
		return segment.contains(x, y);
	}
	
	public PathIterator getPathIterator(AffineTransform at) {
		return segment.getPathIterator(at);
	}
	
	public PathIterator getPathIterator() {
		return segment.getPathIterator(null);
	}

}
