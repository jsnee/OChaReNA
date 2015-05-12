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

/**
 * MasterSegmentHeader class. Manipulates the master segment header file.
 * @author Joe Snee
 *
 */
public class MasterSegmentHeader {
	private static final Logger logger = Logger.getLogger(MasterSegmentHeader.class.getName());

	public static final String masterHeaderFilename = "master" + OcharenaSettings.OCH_HEADER_EXTENSION;

	private static final int NAME_FLAG = 'n';
	private static final int WIDTH_FLAG = 'w';
	private static final int HEIGHT_FLAG = 'h';

	private String originalImageFilename;
	private int originalImageWidth;
	private int originalImageHeight;

	/**
	 * Constructor to load master segment header from archive
	 * @param ochFile
	 */
	public MasterSegmentHeader(File ochFile) {
		readMasterHeader(ochFile);
	}

	/**
	 * Read master header from the och archive
	 * @param ochFile
	 */
	private void readMasterHeader(File ochFile) {
		try {
			ZipFile ochArchive = new ZipFile(ochFile);

			// Loop through all of the entries, skipping them if not the master header file
			for (Enumeration<ZipArchiveEntry> entries = ochArchive.getEntries(); entries.hasMoreElements();) {

				ZipArchiveEntry archiveEntry = entries.nextElement();
				if (!archiveEntry.getName().equalsIgnoreCase(masterHeaderFilename))
					continue;
				InputStream is = ochArchive.getInputStream(archiveEntry);
				String eachLine;

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				// Read the stored data
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
	
	/**
	 * Gets the original image filename
	 * @return
	 */
	public String getOriginalImageFilename() {
		return originalImageFilename;
	}
	
	/**
	 * Gets the original image width
	 * @return
	 */
	public int getOriginalImageWidth() {
		return originalImageWidth;
	}
	
	/**
	 * Gets the original image height
	 * @return
	 */
	public int getOriginalImageHeight() {
		return originalImageHeight;
	}

	/**
	 * Debugging use. Prints header values to console.
	 */
	public void printVals() {
		System.out.println("Filename: " + originalImageFilename);
		System.out.println("\tDimensions: [" + originalImageWidth + ", " + originalImageHeight + "]");
	}
}
