package com.jophus.ocharena.document;

import java.awt.Dimension;

public class OCHMetadata {
	
	private String archiveFilepath;
	private String imageFilename;
	private Dimension imageDimensions;
	
	public OCHMetadata(String archiveFilepath) {
		this.archiveFilepath = archiveFilepath;
	}
	
	public void setArchiveFilepath(String archiveFilepath) {
		this.archiveFilepath = archiveFilepath;
	}
	
	public void setImageFilename(String imageFilename) {
		this.imageFilename = imageFilename;
	}
	
	public void setImageDimensions(Dimension imageDimensions) {
		this.imageDimensions = imageDimensions;
	}
	
	public void setImageDimensions(int imageWidth, int imageHeight) {
		this.imageDimensions = new Dimension(imageWidth, imageHeight);
	}
	
	public String getArchiveFilepath() {
		return archiveFilepath;
	}
	
	public String getImageFilename() {
		return imageFilename;
	}
	
	public Dimension getImageDimensions() {
		return imageDimensions;
	}

}
