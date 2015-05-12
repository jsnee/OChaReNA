package com.jophus.ocharena.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.jophus.ocharena.factory.OCHFileFactory;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PixelPath;
import com.jophus.ocharena.ochfile.LineSegmentHeader;
import com.jophus.ocharena.ochfile.MasterSegmentHeader;

/**
 * Document class. Handles all file manipulation
 * @author Joe Snee
 *
 */
public class OCHDocument {
	private static final Logger logger = Logger.getLogger(OCHDocument.class.getName());

	// Master Header contains image details
	protected MasterSegmentHeader masterHeader;
	// Document image as pixels
	protected ImagePixels imagePixels;
	// .och archive file
	protected OCHFile ochFile;
	// Filepath to the archive file
	protected String filepath;
	
	@Deprecated
	// Mute blue was an attempt to filter out blue lines on lined paper
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
	
	/**
	 * Constructor either reads in the .och archive or generates one from an image
	 * @param archiveFilepath
	 */
	public OCHDocument(String archiveFilepath) {
		// If the file is an image, generate an och file
		if (OCHFile.isValidImageFile(archiveFilepath)) {
			try {
				File archiveFile = OCHFileFactory.buildOCHFromImage(archiveFilepath);
				archiveFilepath = archiveFile.getAbsolutePath();
			} catch (IOException e) {
				logger.severe("Error Creating .och file from image!");
				e.printStackTrace();
			}
		}
		// Make sure the och file is valid and load image
		if (OCHFile.isValidOCHDocumentFile(archiveFilepath)) {
			ochFile = new OCHFile(archiveFilepath);
			filepath = archiveFilepath;
			masterHeader = new MasterSegmentHeader(new File(filepath));
			imagePixels = ochFile.extractImagePixels(masterHeader);
		}
	}
	
	@Deprecated
	// Save line images to the och archive
	public void archiveLines(File lineDirectory) {
		ochFile.addDirectoryToArchive(lineDirectory);
	}
	
	// Save line header file to the och archive
	public void archiveLineHeader(File lineHeader) {
		ochFile.addFileToArchive(lineHeader, LineSegmentHeader.lineSegmentHeaderFilename);
	}
	
	@Deprecated
	// Save char images to the och archive
	public void archiveChars(File charDirectory) {
		ochFile.addDirectoryToArchive(charDirectory);
	}

	// masterHeader getter method
	public MasterSegmentHeader getMasterHeader() {
		return masterHeader;
	}

	// imagePixels getter method
	public ImagePixels getImagePixels() {
		return imagePixels;
	}
	
	// load lines saved to och archive
	public PathManager loadImageHeader() {
		PathManager result = new PathManager(imagePixels.getImageWidth(), imagePixels.getImageHeight());
		try {
			ZipFile ochArchive = new ZipFile(new File(filepath));

			// Loop through all the archive entries, but skip if not the one we want
			for (Enumeration<ZipArchiveEntry> entries = ochArchive.getEntries(); entries.hasMoreElements();) {

				ZipArchiveEntry archiveEntry = entries.nextElement();
				if (!archiveEntry.getName().equalsIgnoreCase(LineSegmentHeader.lineSegmentHeaderFilename))
					continue;
				ObjectInputStream is = new ObjectInputStream(ochArchive.getInputStream(archiveEntry));
				// First integer defines how many lines were saved/to expect
				int length = is.readInt();
				for (int i = 0; i < length; i++) {
					char[] chars = new char[4];
					// First char - newline
					chars[0] = is.readChar();
					// Second char - hyphen
					chars[1] = is.readChar();
					// Third char - path type char
					chars[2] = is.readChar();
					// Fourth char - space
					chars[3] = is.readChar();
					Object o = is.readObject();
					result.addPath(PixelPath.buildPixelPath(chars[2], o));
				}
			}
			ochArchive.close();

		} catch (IOException | ClassNotFoundException e) {
			logger.severe(e.getMessage());
		}
		
		return result;
	}
	
	@Deprecated
	// Load pixels after muting blue
	public ImagePixels loadBlueMutedImagePixels() {
		ImagePixels img = ochFile.extractImagePixels(masterHeader);
		for (int i = 0; i < img.getPixelValues().length; i++) {
			img.setPixel(i, ImagePixels.muteBlue(img.getPixelByIndex(i)));
		}
		return img;
	}

	// filepath getter method
	public String getFilepath() {
		return filepath;
	}
}
