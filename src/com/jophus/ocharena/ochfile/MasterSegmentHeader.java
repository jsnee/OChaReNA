package com.jophus.ocharena.ochfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.OCHFile;

public class MasterSegmentHeader {
	private static final Logger logger = Logger.getLogger(MasterSegmentHeader.class.getName());

	public static final String masterHeaderFilename = "master" + OcharenaSettings.OCH_HEADER_EXTENSION;

	private static final int NAME_FLAG = 'n';
	private static final int WIDTH_FLAG = 'w';
	private static final int HEIGHT_FLAG = 'h';

	private String originalImageFilename;
	private int originalImageWidth;
	private int originalImageHeight;

	public MasterSegmentHeader(File ochFile) {
		readMasterHeader(ochFile);
	}

	private void readMasterHeader(File ochFile) {
		try {
			ZipFile ochArchive = new ZipFile(ochFile);

			for (Enumeration<ZipArchiveEntry> entries = ochArchive.getEntries(); entries.hasMoreElements();) {

				ZipArchiveEntry archiveEntry = entries.nextElement();
				if (!archiveEntry.getName().equalsIgnoreCase(masterHeaderFilename))
					continue;
				InputStream is = ochArchive.getInputStream(archiveEntry);
				String eachLine;

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				while ((eachLine = br.readLine()) != null) {
					if (eachLine.isEmpty()) {
						continue;
					}
					switch ((int)eachLine.charAt(1)) {
					case NAME_FLAG:
						originalImageFilename = eachLine.substring(3);
						break;
					case WIDTH_FLAG:
						originalImageWidth = Integer.parseInt(eachLine.substring(3));
						break;
					case HEIGHT_FLAG:
						originalImageHeight = Integer.parseInt(eachLine.substring(3));
						break;
					}
				} 
			}
			ochArchive.close();

		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
	
	public String getOriginalImageFilename() {
		return originalImageFilename;
	}
	
	public int getOriginalImageWidth() {
		return originalImageWidth;
	}
	
	public int getOriginalImageHeight() {
		return originalImageHeight;
	}

	public void printVals() {
		System.out.println("Filename: " + originalImageFilename);
		System.out.println("\tDimensions: [" + originalImageWidth + ", " + originalImageHeight + "]");
	}
}
