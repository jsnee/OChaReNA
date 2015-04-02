package com.jophus.ocharena.document;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.factory.OCHFileFactory;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.ochfile.MasterSegmentHeader;

public class OCHDocument {
	private static final Logger logger = Logger.getLogger(OCHDocument.class.getName());

	protected MasterSegmentHeader masterHeader;
	protected ImagePixels imagePixels;
	protected OCHFile ochFile;
	protected String filepath;
	
	public OCHDocument(String archiveFilepath, boolean muteBlue) {
		if (OCHFile.isValidImageFile(archiveFilepath)) {
			try {
				File archiveFile = OCHFileFactory.buildOCHFromImage(archiveFilepath);
				archiveFilepath = archiveFile.getAbsolutePath();
			} catch (IOException e) {
				logger.severe("Error Creating .och file from image!");
				e.printStackTrace();
			}
		}
		if (OCHFile.isValidOCHDocumentFile(archiveFilepath)) {
			ochFile = new OCHFile(archiveFilepath);
			filepath = archiveFilepath;
			masterHeader = new MasterSegmentHeader(new File(filepath));
			imagePixels = ochFile.extractImagePixels(masterHeader, muteBlue);
		}
	}
	
	public OCHDocument(String archiveFilepath) {
		if (OCHFile.isValidImageFile(archiveFilepath)) {
			try {
				File archiveFile = OCHFileFactory.buildOCHFromImage(archiveFilepath);
				archiveFilepath = archiveFile.getAbsolutePath();
			} catch (IOException e) {
				logger.severe("Error Creating .och file from image!");
				e.printStackTrace();
			}
		}
		if (OCHFile.isValidOCHDocumentFile(archiveFilepath)) {
			ochFile = new OCHFile(archiveFilepath);
			filepath = archiveFilepath;
			masterHeader = new MasterSegmentHeader(new File(filepath));
			imagePixels = ochFile.extractImagePixels(masterHeader);
		}
	}

	public MasterSegmentHeader getMasterHeader() {
		return masterHeader;
	}

	public ImagePixels getImagePixels() {
		return imagePixels;
	}
	
	public ImagePixels loadBlueMutedImagePixels() {
		ImagePixels img = ochFile.extractImagePixels(masterHeader);
		for (int i = 0; i < img.getPixelValues().length; i++) {
			img.setPixel(i, ImagePixels.muteBlue(img.getPixelByIndex(i)));
		}
		return img;
	}

	public String getFilepath() {
		return filepath;
	}
}
