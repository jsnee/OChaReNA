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
	
	public void archiveLines(File lineDirectory) {
		ochFile.addDirectoryToArchive(lineDirectory);
	}
	
	public void archiveLineHeader(File lineHeader) {
		ochFile.addFileToArchive(lineHeader, LineSegmentHeader.lineSegmentHeaderFilename);
	}
	
	public void archiveChars(File charDirectory) {
		ochFile.addDirectoryToArchive(charDirectory);
	}

	public MasterSegmentHeader getMasterHeader() {
		return masterHeader;
	}

	public ImagePixels getImagePixels() {
		return imagePixels;
	}
	
	public PathManager loadImageHeader() {
		PathManager result = new PathManager(imagePixels.getImageWidth(), imagePixels.getImageHeight());
		try {
			ZipFile ochArchive = new ZipFile(new File(filepath));

			for (Enumeration<ZipArchiveEntry> entries = ochArchive.getEntries(); entries.hasMoreElements();) {

				ZipArchiveEntry archiveEntry = entries.nextElement();
				if (!archiveEntry.getName().equalsIgnoreCase(LineSegmentHeader.lineSegmentHeaderFilename))
					continue;
				ObjectInputStream is = new ObjectInputStream(ochArchive.getInputStream(archiveEntry));
				int length = is.readInt();
				for (int i = 0; i < length; i++) {
					char[] chars = new char[4];
					chars[0] = is.readChar();
					chars[1] = is.readChar();
					chars[2] = is.readChar();
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
